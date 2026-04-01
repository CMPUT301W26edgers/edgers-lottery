package com.example.edgers_lottery.services;

import com.example.edgers_lottery.models.Comment;

import java.util.ArrayList;

/**
 * Callback interface for receiving a list of {@link Comment} objects asynchronously.
 */
public interface CommentCallback {

    /**
     * Called when the comment operation has completed.
     * @param comments the retrieved list of comments
     */
    void onComplete(ArrayList<Comment> comments);
}