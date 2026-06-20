package com.javaprep.backend.service;

import com.javaprep.backend.entity.UserNote;
import java.util.Optional;

public interface UserNoteService {
    Optional<UserNote> getNote(String userId, String targetId);
    UserNote saveOrUpdateNote(String userId, String targetId, UserNote request);
    void deleteNote(String userId, String targetId);
}