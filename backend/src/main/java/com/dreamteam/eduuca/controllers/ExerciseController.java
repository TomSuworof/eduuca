package com.dreamteam.eduuca.controllers;

import com.dreamteam.eduuca.entities.Exercise;
import com.dreamteam.eduuca.entities.ExerciseState;
import com.dreamteam.eduuca.payload.request.ExerciseUploadRequest;
import com.dreamteam.eduuca.payload.response.ExerciseDTO;
import com.dreamteam.eduuca.payload.response.PageResponseDTO;
import com.dreamteam.eduuca.services.ExerciseEditorService;
import com.dreamteam.eduuca.services.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Log4j2
@Controller
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final ExerciseEditorService exerciseEditorService;

    @GetMapping("/get")
    @ResponseBody
    public ResponseEntity<PageResponseDTO<ExerciseDTO>> getExercisesPaginated(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        log.debug("getExercisesPaginated() called. Limit={}, offset={}", limit, offset);
        PageResponseDTO<ExerciseDTO> response = exerciseService.getPageWithExercisesByState(ExerciseState.PUBLISHED, limit, offset);
        log.trace("getExercisesPaginated(). Response to send: {}", () -> response);

        if (!response.isHasBefore() && !response.isHasAfter()) {
            log.trace("getExercisesPaginated(). Response contains all exercises. Response status is OK");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.trace("getExercisesPaginated(). Response does not contain all exercises. Response status is PARTIAL_CONTENT");
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<ExerciseDTO> getExercise(@PathVariable UUID id) {
        log.debug("getExercise() called. ID to search: {}", id);
        Exercise exercise = exerciseService.getExerciseById(id);
        log.trace("getExercise(). Exercise to return: {}", () -> exercise);
        return ResponseEntity.ok().body(new ExerciseDTO(exercise));
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<ExerciseDTO> uploadExercise(@RequestBody ExerciseUploadRequest exercise, @RequestParam String action) {
        log.debug("uploadExercise() called. Exercise: {}, action: {}", exercise, action);
        ExerciseDTO exerciseDTO = exerciseEditorService.uploadExercise(exercise, ExerciseState.getFromAction(action));
        log.trace("uploadExercise(). Result exercise DTO: {}", () -> exerciseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void deleteExercise(@PathVariable UUID id) {
        log.debug("deleteExercise() called. ID: {}", id);
        exerciseService.deleteExerciseById(id);
    }
}
