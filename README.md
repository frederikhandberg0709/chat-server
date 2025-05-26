# Chat server

As a portfolio project, I have decided to build a chat application similar to Facebook Messenger.

This repository serves as the backend for the chat application.
There is going to be a repository exclusively for the frontend, which will include a web application and possibly a mobile app as well.

I have only recently started working on the backend for this project, so there is still much work to be done.

The backend REST server is being built using Spring Boot, as it is a popular choice and I have some experience with it already from college.

I will be building another server. Possibly a SignalR server to handle instant messaging and real-time notifications.

## Functional Requirements

The functional requirements listed below define the core features the chat application should support.

1. As a user, I want to be able to create an account, so that I can send and receive messages.

2. As a user, I want to be able to receive messages in real-time, so that I don’t miss any messages or need to reload the page.

3. As a user, I want to be able to react to messages with emojis, so that I can quickly express my feelings or feedback without having to type a response.

4. As a user, I want to be able to delete my messages, so that I can remove messages I regret sending.

5. As a user, I want to be able to block other users, so that I can stop receiving messages from people I don't want to interact with.

6. As a user, I want to be able to edit my past messages, so that I can fix grammar mistakes or clarify what I meant.

7. As a user, I want to be able to tag other users in my messages in group chats, so that they can easily see messages directed at them.

8. As a user, I want to reply to specific messages in a chat, **so that** I can provide context and make conversations easier to follow.

## Product Backlog

The product backlog lists features based on the functional requirements of the chat application.
It is divided into three sections to reflect their development status:

1. **Implemented:** Features from the functional requirements that are fully developed and working.

2. **In Progress:** Features currently being developed or tested.

3. **Planned:** Features identified from the functional requirements, but development not yet started.

### ✅ Implemented

1. Account creation and authentication

### 🚧 In Progress

1. Real-time message delivery (no reload required)

### 🔜 Planned

1. Emoji reactions on messages

2. Message deletion

3. Block users

4. Edit sent messages

5. Tag users in group chats (e.g. `@username`)

6. Reply to specific messages in chat (with quoted message context)
