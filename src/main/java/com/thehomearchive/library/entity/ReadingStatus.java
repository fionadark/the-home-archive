package com.thehomearchive.library.entity;

/**
 * Reading status for books in personal library.
 * Matches the database ENUM('UNREAD', 'READING', 'READ', 'DNF').
 */
public enum ReadingStatus {
    UNREAD,
    READING,
    READ,
    DNF
}