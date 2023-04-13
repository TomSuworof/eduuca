package com.dreamteam.eduuca.controllers;

import com.dreamteam.eduuca.payload.common.Input;
import com.dreamteam.eduuca.payload.response.ExerciseDTO;
import com.dreamteam.eduuca.payload.response.PageResponseDTO;
import com.dreamteam.eduuca.payload.response.TagResponse;
import com.dreamteam.eduuca.services.ModelService;
import com.dreamteam.eduuca.services.TagService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Log4j2
@Controller
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final ModelService modelService;
    private final TagService tagService;

    @PostMapping("/suggest")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<TagResponse> suggestTags(@RequestBody Input input) {
        log.debug("suggestTags() called. Request: {}", () -> input);
        List<String> tags = modelService.predictTags(input.text());
        TagResponse response = new TagResponse(input.text(), tags);
        log.trace("suggestTags(). Response to send: {}", () -> response);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/get_exercises")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<PageResponseDTO<ExerciseDTO>> getExercisesByTags(
            @RequestBody List<String> tags,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        log.debug("getExercisesByTags() called. Tags: {}", () -> tags);
        PageResponseDTO<ExerciseDTO> response = tagService.getPageWithExercises(tags, limit, offset);
        log.trace("getExercisesByTags(). Response: {}", () -> response);
        return ResponseEntity.ok().body(response);
    }
}