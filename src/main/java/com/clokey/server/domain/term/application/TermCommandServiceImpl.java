package com.clokey.server.domain.term.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.RegisterStatus;
import com.clokey.server.domain.term.converter.TermConverter;
import com.clokey.server.domain.term.domain.entity.MemberTerm;
import com.clokey.server.domain.term.domain.entity.Term;
import com.clokey.server.domain.term.dto.TermRequestDTO;
import com.clokey.server.domain.term.dto.TermResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermCommandServiceImpl implements TermCommandService {

    private final MemberRepositoryService memberRepositoryService;
    private final TermRepositoryService termRepositoryService;
    private final MemberTermRepositoryService memberTermRepositoryService;

    private static final String APP_VERSION = "1.0.0";

    @Override
    @Transactional
    public TermResponseDTO joinTerm(Long userId, TermRequestDTO.Join request) {
        // 사용자 조회
        Member member = memberRepositoryService.findMemberById(userId);

        // 기존 동의 약관 조회
        List<MemberTerm> existingMemberTerms = memberTermRepositoryService.findByMember(member);
        Set<Long> existingTermIds = existingMemberTerms.stream()
                .map(memberTerm -> memberTerm.getTerm().getId())
                .collect(Collectors.toSet());

        //새로 들어온 약관 동의 목록
        Set<Long> newTermIds = request.getTerms().stream()
                .filter(TermRequestDTO.Join.Term::getAgreed)
                .map(TermRequestDTO.Join.Term::getTermId)
                .collect(Collectors.toSet());

        // 삭제 대상: 기존에 있었는데 요청에는 없는 약관
        Set<Long> toDelete = new HashSet<>(existingTermIds);
        toDelete.removeAll(newTermIds);

        // 추가 대상: 요청에는 있는데 기존에 없던 약관
        Set<Long> toAdd = new HashSet<>(newTermIds);
        toAdd.removeAll(existingTermIds);

        // 삭제 처리
        for (Long termId : toDelete) {
            memberTermRepositoryService.deleteAllByMemberIdAndTermId(userId, termId);
        }

        // 추가 처리
        List<TermResponseDTO.Term> termResponses = new ArrayList<>();
        for (Long termId : toAdd) {
            Term term = termRepositoryService.findById(termId);
            MemberTerm memberTerm = MemberTerm.builder()
                    .member(member)
                    .term(term)
                    .build();
            memberTermRepositoryService.save(memberTerm);

            termResponses.add(TermResponseDTO.Term.builder()
                    .termId(term.getId())
                    .agreed(true)
                    .build());
        }

        // 이미 있던 항목들도 응답에 포함시키기 위해 전체 목록 다시 구성
        for (Long termId : newTermIds) {
            if (toAdd.contains(termId)) continue; // 이미 포함됨
            Term term = termRepositoryService.findById(termId);
            termResponses.add(TermResponseDTO.Term.builder()
                    .termId(term.getId())
                    .agreed(true)
                    .build());
        }

        // 회원 상태 업데이트
        if (member.getRegisterStatus() == RegisterStatus.NOT_AGREED) {
            member.updateRegisterStatus(RegisterStatus.AGREED_PROFILE_NOT_SET);
            memberRepositoryService.saveMember(member);
        }

        // 최종 응답
        return TermResponseDTO.builder()
                .userId(userId)
                .terms(termResponses)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TermResponseDTO.TermList> getTerms() {
        List<Term> terms = termRepositoryService.findAll();  // 모든 약관 조회

        List<TermResponseDTO.TermList> termList = new ArrayList<>();
        for (Term term : terms) {
            termList.add(TermConverter.toTermListDto(term));
        }

        return termList;
    }

    @Override
    @Transactional(readOnly = true)
    public TermResponseDTO.UserAgreementDTO getOptionalTerms(Long userId) {
        // 사용자 조회
        Member member = memberRepositoryService.findMemberById(userId);

        // 사용자가 동의한 약관 조회
        List<MemberTerm> memberTerms = memberTermRepositoryService.findByMember(member);

        // 사용자가 동의한 약관 ID 목록
        Set<Long> agreedTermIds = memberTerms.stream()
                .map(memberTerm -> memberTerm.getTerm().getId())
                .collect(Collectors.toSet());

        // 전체 선택 약관 조회 (optional = true)
        List<Term> optionalTerms = termRepositoryService.findByOptionalTrue();

        // OptionalTermDTO 리스트 생성
        List<TermResponseDTO.OptionalTermDTO> termResponses = optionalTerms.stream()
                .map(term -> TermResponseDTO.OptionalTermDTO.builder()
                        .termId(term.getId())  // 약관 ID
                        .title(term.getTitle())  // 약관 제목
                        .agreed(agreedTermIds.contains(term.getId())) // 사용자가 동의했는지 여부 판단
                        .build())
                .collect(Collectors.toList());

        return TermResponseDTO.UserAgreementDTO.builder()
                .socialType(member.getSocialType().toString())  // 소셜 타입 추가
                .email(member.getEmail()) // 이메일 추가
                .appVersion("1.0.0") // 앱 버전 추가 (필드 확인 필요)
                .terms(termResponses)  // OptionalTermDTO 리스트 반환
                .build();
    }

    @Override
    @Transactional
    public TermResponseDTO.UserAgreementDTO optionalTermAgree(Long userId, TermRequestDTO.Join request) {
        // 사용자 조회
        Member member = memberRepositoryService.findMemberById(userId);

        // 약관 동의 처리
        List<TermResponseDTO.OptionalTermDTO> termResponses = new ArrayList<>();
        for (TermRequestDTO.Join.Term termDto : request.getTerms()) {
            // 약관 조회
            Term term = termRepositoryService.findById(termDto.getTermId());

            if (termDto.getAgreed()) {
                // 동의한 경우 -> 저장
                MemberTerm memberTerm = MemberTerm.builder()
                        .member(member)
                        .term(term)
                        .build();
                memberTermRepositoryService.save(memberTerm);
            } else {
                // 동의 철회한 경우 -> 삭제
                memberTermRepositoryService.deleteAllByMemberIdAndTermId(userId, term.getId());
            }

            // 응답 데이터 생성
            termResponses.add(TermResponseDTO.OptionalTermDTO.builder()
                    .termId(term.getId())  // 약관 ID
                    .title(term.getTitle())  // 약관 제목
                    .agreed(termDto.getAgreed())  // 실제 동의 여부 반영
                    .build());
        }

        // 최종 응답 생성
        return TermResponseDTO.UserAgreementDTO.builder()
                .socialType(member.getSocialType().toString())  // 소셜 타입 추가
                .email(member.getEmail()) // 이메일 추가
                .appVersion(APP_VERSION) // 앱 버전 추가
                .terms(termResponses)  // OptionalTermDTO 리스트 반환
                .build();
    }


}
