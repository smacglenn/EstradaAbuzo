package com.example.androiddrinkzlyboozeclient.Callback;

import com.example.androiddrinkzlyboozeclient.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
