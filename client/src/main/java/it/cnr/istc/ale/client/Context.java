/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.ale.client;

import it.cnr.istc.ale.api.Lesson;
import it.cnr.istc.ale.api.User;
import it.cnr.istc.ale.api.messages.Event;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Context {

    public static final String HOST = "localhost";
    public static final String SERVICE_PORT = "8080";
    public static final String REST_URI = "http://" + HOST + ":" + SERVICE_PORT;
    public static final String MQTT_PORT = "1883";
    private static Context context;

    public static Context getContext() {
        if (context == null) {
            context = new Context();
        }
        return context;
    }
    private final Client client = ClientBuilder.newClient();
    private final UserResource ur = new UserResource(client);
    private final LessonResource lr = new LessonResource(client);
    private Stage stage;
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<Lesson> following_lessons = FXCollections.observableArrayList();
    private final ObservableList<User> teachers = FXCollections.observableArrayList();
    private final ObservableList<Lesson> lessons = FXCollections.observableArrayList();
    private final ObservableList<User> students = FXCollections.observableArrayList();

    private Context() {
    }

    public Client getClient() {
        return client;
    }

    public UserResource getUserResource() {
        return ur;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ObjectProperty<User> getUser() {
        return user;
    }

    public ObservableList<Event> getEvents() {
        return events;
    }

    public ObservableList<Lesson> getFollowingLessons() {
        return following_lessons;
    }

    public ObservableList<User> getTeachers() {
        return teachers;
    }

    public ObservableList<Lesson> getLessons() {
        return lessons;
    }

    public ObservableList<User> getStudents() {
        return students;
    }

    public void login(String email, String password) {
        set_user(ur.login(email, password));
    }

    public void new_user(String email, String password, String first_name, String last_name) {
        set_user(ur.new_user(email, password, first_name, last_name));
    }

    public void logout() {
        set_user(null);
    }

    private void set_user(User u) {
        user.set(u);
        if (u != null) {
            following_lessons.addAll(lr.get_followed_lessons(u.getId()));
            teachers.addAll(ur.get_teachers(u.getId()));
            lessons.addAll(lr.get_lessons(u.getId()));
            students.addAll(ur.get_students(u.getId()));
        } else {
            following_lessons.clear();
            teachers.clear();
            lessons.clear();
            students.clear();
        }
    }

    public void add_teacher(User teacher) {
        ur.add_teacher(user.get().getId(), teacher.getId());
        teachers.add(teacher);
    }

    public void remove_teacher(User teacher) {
        ur.remove_teacher(user.get().getId(), teacher.getId());
        teachers.remove(teacher);
    }
}
