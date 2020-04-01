package com.example.helpgiver.controller;

import com.example.helpgiver.mongo.HelpRequestRepository;
import com.example.helpgiver.mongo.UserRepository;
import com.example.helpgiver.objects.HelpRequest;
import com.example.helpgiver.objects.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class HelpRequestController {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    // TODO help requests should link to users (but links are generally meh now)

    @GetMapping("helpRequests")
    public ResponseEntity<CollectionModel<EntityModel<HelpRequest>>> getHelpRequests() {
        List<EntityModel<HelpRequest>> helpRequestEntities = StreamSupport.stream(helpRequestRepository.findAll().spliterator(), false)
                .map(helpRequest -> new EntityModel<>(helpRequest,
                        linkTo(methodOn(HelpRequestController.class).getHelpRequest(helpRequest.getId())).withSelfRel(),
                        linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new CollectionModel<>(helpRequestEntities,
                        linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withSelfRel()));
    }

    @GetMapping("helpRequest/{id}")
    public ResponseEntity<EntityModel<HelpRequest>> getHelpRequest(@PathVariable String id) {
        return helpRequestRepository.findById(id)
                .map(helpRequest -> new EntityModel<>(helpRequest,
                        linkTo(methodOn(HelpRequestController.class).getHelpRequest(helpRequest.getId())).withSelfRel(),
                        linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("helpRequest")
    public ResponseEntity<EntityModel<HelpRequest>> createHelpRequest(@RequestParam HelpRequest helpRequest) {
        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        return ResponseEntity.ok(new EntityModel<>(savedHelpRequest,
                linkTo(methodOn(HelpRequestController.class).getHelpRequest(savedHelpRequest.getId())).withSelfRel(),
                linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")));
    }

    @PutMapping("helpRequest")
    public ResponseEntity<EntityModel<HelpRequest>> updateHelpRequest(@RequestParam HelpRequest helpRequest) {
        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        return ResponseEntity.ok(new EntityModel<>(savedHelpRequest,
                linkTo(methodOn(HelpRequestController.class).getHelpRequest(savedHelpRequest.getId())).withSelfRel(),
                linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")));
    }

    @PostMapping("assignRequest")
    public ResponseEntity<EntityModel<HelpRequest>> addHandler(@RequestParam @NotNull String id, @RequestParam @NotNull String userId) {
        Optional<HelpRequest> helpRequest = helpRequestRepository.findById(id);
        Optional<User> user = userRepository.findById(userId);

        if (!helpRequest.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Help request not found");
        }
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }

        HelpRequest foundHelpRequest = helpRequest.get();
        foundHelpRequest.getHelper().add(user.get());
        helpRequestRepository.save(foundHelpRequest);

        return ResponseEntity.ok(new EntityModel<>(foundHelpRequest,
                linkTo(methodOn(HelpRequestController.class).getHelpRequest(foundHelpRequest.getId())).withSelfRel(),
                linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")));
    }

    @DeleteMapping("helpRequest")
    public ResponseEntity<CollectionModel<Object>> deleteHelpRequest(@RequestParam HelpRequest helpRequest) {
        helpRequestRepository.delete(helpRequest);

        return ResponseEntity.ok(new CollectionModel<>(Collections.emptySet(),
                linkTo(methodOn(HelpRequestController.class).getHelpRequests()).withRel("helpRequests")));
    }
}
