package com.frederikhandberg.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frederikhandberg.service.ChatMessageService;
import com.frederikhandberg.service.GroupChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/group-chats")
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final ChatMessageService messageService;
}
