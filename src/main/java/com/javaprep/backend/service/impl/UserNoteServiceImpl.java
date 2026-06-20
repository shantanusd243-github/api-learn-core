package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.UserNote;
import com.javaprep.backend.repository.UserNoteRepository;
import com.javaprep.backend.service.UserNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserNoteServiceImpl implements UserNoteService {

    private final UserNoteRepository userNoteRepository;

    @Override
    public Optional<UserNote> getNote(String userId, String targetId) {
        return userNoteRepository.findByUserIdAndTargetId(userId, targetId);
    }

    @Override
    public UserNote saveOrUpdateNote(String userId, String targetId, UserNote request) {
        Optional<UserNote> existingNoteOpt = userNoteRepository.findByUserIdAndTargetId(userId, targetId);
        
        UserNote noteToSave;
        if (existingNoteOpt.isPresent()) {
            noteToSave = existingNoteOpt.get();
            noteToSave.setNoteText(request.getNoteText());
            noteToSave.setUpdatedAt(LocalDateTime.now());
        } else {
            noteToSave = UserNote.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .noteText(request.getNoteText())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        return userNoteRepository.save(noteToSave);
    }

    @Override
    public void deleteNote(String userId, String targetId) {
        userNoteRepository.findByUserIdAndTargetId(userId, targetId)
                .ifPresent(userNoteRepository::delete);
    }
}