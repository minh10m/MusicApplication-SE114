package com.music.application.be.modules.role;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Role {
    USER, ADMIN
}
