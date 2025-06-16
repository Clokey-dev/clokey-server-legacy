package com.clokey.server.domain.folder.exception.validator;

import com.clokey.server.domain.folder.domain.repository.FolderRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.folder.domain.entity.Folder;
import com.clokey.server.domain.folder.exception.FolderException;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class FolderAccessibleValidator {

    private final FolderRepository folderRepository;

    public Folder validateFolderAccessOfMember(Long folderId, Long memberId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(()->new FolderException(ErrorStatus.NO_SUCH_FOLDER));

        //접근 권한 확인 - 나의 폴더가 아닐 경우 접근 불가.
        boolean isMyFolder = folder.getMember().getId().equals(memberId);

        if (!isMyFolder) {
            throw new FolderException(ErrorStatus.NO_PERMISSION_TO_ACCESS_FOLDER);
        }

        return folder;
    }
}
