package com.project.public_safety_app.service;

import com.project.public_safety_app.dto.CommentRequest;
import com.project.public_safety_app.model.Comment;
import com.project.public_safety_app.model.Discussion;
import com.project.public_safety_app.model.User;
import com.project.public_safety_app.repository.CommentRepository;
import com.project.public_safety_app.repository.DiscussionRepository;
import com.project.public_safety_app.repository.UserRepository;
import com.project.public_safety_app.util.EntityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    public Discussion createDiscussion(Discussion discussion, User user) {
        discussion.setComments(null);
        User user1 = EntityUtil.convertToEntity(userService.getUserByName(user.getUserName()));

        // Add the discussion to the user's discussion list
        if (user1.getDiscussions() == null) {
            user1.setDiscussions(new ArrayList<>(List.of(discussion))); // Create a new list if necessary
        } else {
            user1.getDiscussions().add(discussion); // Add the discussion to the existing list
        }

        // Set the user for the discussion (this may be redundant if cascade is set)
        discussion.setUser(user1);

        // Save the updated user (this will also save the discussion if CascadeType.ALL is used)
        userRepository.save(user1);  // Save the user and its discussions

        return discussion;
    }

    public List<Discussion> getAllDiscussions() {
        return discussionRepository.findAll();
    }

    public List<Discussion> getDiscussionsByUser(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            return List.of(); // Return an empty list if the user is not found
        }
        return user.getDiscussions(); // Return the discussions for the user
    }

    public void deleteDiscussionByName(String userName, long id) {
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Find the discussion by ID
        Discussion discussion = discussionRepository.findById(id).orElse(null);
        if (discussion == null) {
            throw new RuntimeException("Discussion not found");
        }

        // Remove the discussion from the user's list of discussions
        user.getDiscussions().remove(discussion);
        userRepository.save(user);  // Save the updated user (if needed, depending on your cascading setup)

        // Delete all comments associated with the discussion
        List<Comment> comments = discussion.getComments();
        if (comments != null) {
            for (Comment comment : comments) {
                commentRepository.delete(comment);
            }
        }

        // Delete the discussion
        discussionRepository.deleteById(id);
    }

    public Comment addComment(Long discussionId, CommentRequest commentRequest) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null) {
            return null;  // Discussion not found
        }

        Comment comment = new Comment();
        comment.setDiscussion(discussion);
        comment.setContent(commentRequest.getContent());
        commentRepository.save(comment);

        return comment;
    }

    // Get all comments for a discussion
    public List<Comment> getCommentsByDiscussionId(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null) {
            return List.of();  // Discussion not found
        }
        return commentRepository.findByDiscussionId(discussionId);
    }

    // Upvote a discussion
    public boolean upvoteDiscussion(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null || discussion.isUpvoted()) {
            return false;  // Discussion not found or already upvoted
        }

        discussion.setUpvotes(discussion.getUpvotes() + 1);
        discussion.setUpvoted(true);  // Mark as upvoted
        discussionRepository.save(discussion);

        return true;
    }

    // Downvote a discussion
    public boolean downvoteDiscussion(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null || discussion.isDownvoted()) {
            return false;  // Discussion not found or already downvoted
        }

        discussion.setDownvotes(discussion.getDownvotes() + 1);
        discussion.setDownvoted(true);  // Mark as downvoted
        discussionRepository.save(discussion);

        return true;
    }
}