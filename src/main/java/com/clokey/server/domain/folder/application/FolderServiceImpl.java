package com.clokey.server.domain.folder.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.cloth.exception.validator.ClothAccessibleValidator;
import com.clokey.server.domain.folder.converter.FolderConverter;
import com.clokey.server.domain.folder.domain.repository.ClothFolderRepository;
import com.clokey.server.domain.folder.domain.repository.FolderRepository;
import com.clokey.server.domain.folder.dto.FolderRequestDTO;
import com.clokey.server.domain.folder.dto.FolderResponseDTO;
import com.clokey.server.domain.folder.exception.FolderException;
import com.clokey.server.domain.folder.domain.entity.Folder;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.folder.domain.entity.ClothFolder;
import com.clokey.server.domain.folder.exception.validator.FolderAccessibleValidator;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.global.error.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final MemberRepositoryService memberRepositoryService;
    private final ClothFolderRepository clothFolderRepository;
    private final ClothRepository clothRepository;

    private final FolderAccessibleValidator folderAccessibleValidator;
    private final ClothAccessibleValidator clothAccessibleValidator;


    @Override
    @Transactional
    public Folder createAndUpdateFolder(Long memberId, FolderRequestDTO.FolderCreateRequest request) {
        Member member = memberRepositoryService.findMemberById(memberId);
        Folder folder = getFolder(request.getFolderId(), member, request);
        folder.rename(request.getFolderName());
        updateClothesToFolder(folder, request.getClothIds(), memberId);
        return folder;
    }

    private Folder getFolder(Long folderId, Member member, FolderRequestDTO.FolderCreateRequest request) {
        if (folderId == null) {
            Folder newFolder = FolderConverter.toFolder(request, member);
            folderRepository.save(newFolder);
            return newFolder;
        }
        Folder folder = folderRepository.findById(folderId).orElseThrow(()-> new FolderException(ErrorStatus.NO_SUCH_FOLDER));
        folderAccessibleValidator.validateFolderAccessOfMember(folderId, member.getId());
        return folder;
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId, Long memberId) {
        folderAccessibleValidator.validateFolderAccessOfMember(folderId, memberId);
        try {
            clothFolderRepository.deleteAllByFolderId(folderId);
            folderRepository.deleteById(folderId);
        } catch (Exception ex) {
            throw new FolderException(ErrorStatus.FAILED_TO_DELETE_FOLDER);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponseDTO.FolderClothesResult getClothesFromFolder(Long folderId, Integer page, Long memberId) {
        Folder folder = folderAccessibleValidator.validateFolderAccessOfMember(folderId, memberId);
        Pageable pageable = PageRequest.of(page - 1, 12);
        Page<ClothFolder> clothFolders = clothFolderRepository.findAllByFolderId(folder.getId(), pageable);
        return FolderConverter.toFolderClothesDTO(clothFolders);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponseDTO.FoldersResult getFolders(Integer page, Long memberId) {
        Member member = memberRepositoryService.findMemberById(memberId);
        Pageable pageable = PageRequest.of(page - 1, 12);
        Page<Folder> folders = folderRepository.findAllByMemberId(member.getId(), pageable);
        List<Long> folderIds = folders.stream().map(Folder::getId).toList();
        List<Object[]> results = clothFolderRepository.findClothImageUrlsFromFolderIds(folderIds);
        Map<Long, String> folderImageMap = results.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (String) row[1]));
        return FolderConverter.toFoldersDTO(folders, folderImageMap);
    }

    private List<Cloth> validateClothesExistAndAccessible(List<Long> clothIds, Long memberId) {
        clothIds.forEach(clothId -> {
            if (!clothRepository.existsById(clothId)) {
                throw new FolderException(ErrorStatus.NO_SUCH_CLOTH);
            }
        });
        List<Cloth> clothes = clothRepository.findAllById(clothIds);
        clothAccessibleValidator.validateClothOfMember(
                clothes.stream().map(Cloth::getId).collect(Collectors.toList()), memberId
        );
        return clothes;
    }

    private void updateClothesToFolder(Folder folder, List<Long> clothIds, Long memberId) {
        List<ClothFolder> existingClothFolders = clothFolderRepository.findAllByFolderId(folder.getId(), Pageable.unpaged()).toList();

        List<Long> existingClothIds = existingClothFolders.stream()
                .map(cf -> cf.getCloth().getId())
                .toList();

        List<Long> toRemove = existingClothIds.stream()
                .filter(id -> !clothIds.contains(id))
                .toList();

        List<Long> toAdd = clothIds.stream()
                .filter(id -> !existingClothIds.contains(id))
                .toList();

        if (!toRemove.isEmpty()) {
            clothFolderRepository.deleteAllByClothIdInAndFolderId(toRemove, folder.getId());
        }

        if (!toAdd.isEmpty()) {
            List<Cloth> clothesToAdd = validateClothesExistAndAccessible(toAdd, memberId);
            List<ClothFolder> clothFolders = clothesToAdd.stream()
                    .map(cloth -> new ClothFolder(cloth, folder))
                    .collect(Collectors.toList());
            clothFolderRepository.saveAll(clothFolders);
        }

        folder.setItemCount((long) clothIds.size());
        folderRepository.save(folder);
    }
}
