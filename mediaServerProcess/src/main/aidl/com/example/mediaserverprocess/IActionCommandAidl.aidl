package com.example.mediaserverprocess;

interface IActionCommandAidl {
    void play();

    void pause();

    void playOrPause();

    void playIndex(int listIndex);

    void next();

    void pre();

    void stop();
}