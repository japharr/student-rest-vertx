package com.japharr.studentrest.entity;

import java.util.concurrent.atomic.AtomicInteger;

public class Student {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final int id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String course;

    public Student() {
        this.id = COUNTER.getAndIncrement();
    }

    public Student(String firstName, String lastName, Integer age, String course) {
        this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.course = course;
    }

    public Student(String firstName, String lastName, Integer age) {
        this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public Student(String firstName, Integer age) {
        this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
