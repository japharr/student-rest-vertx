package com.japharr.studentrest.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import io.vertx.sqlclient.templates.annotations.TemplateParameter;

import java.util.concurrent.atomic.AtomicInteger;

@DataObject
@RowMapped
@ParametersMapped()
public class Student {
    //private static final AtomicInteger COUNTER = new AtomicInteger();

    private Integer id;
    @Column(name = "first_name")
    @TemplateParameter(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    @TemplateParameter(name = "last_name")
    private String lastName;
    private Integer age;
    private String course;

    public Student() {
        //this.id = COUNTER.getAndIncrement();
    }

    public Student(int id) {
        this.id = id;
    }

    public Student(JsonObject json) {
        this.id = json.getInteger("id");
        this.firstName = json.getString("first_name");
        this.lastName = json.getString("last_name");
        this.age = json.getInteger("age");
        this.course = json.getString("course");
    }

    public Student(int id, String firstName, String lastName, Integer age, String course) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.course = course;
    }

    public Student(String firstName, String lastName, Integer age, String course) {
        //this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.course = course;
    }

    public Student(String firstName, String lastName, Integer age) {
        //this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public Student(String firstName, Integer age) {
        //this.id = COUNTER.getAndIncrement();
        this.firstName = firstName;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
