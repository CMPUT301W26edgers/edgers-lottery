package com.example.edgers_lottery.services;

import com.example.edgers_lottery.models.Comment;

import java.util.ArrayList;

public interface CommentCallback {
    void onComplete(ArrayList<Comment> comments);
}