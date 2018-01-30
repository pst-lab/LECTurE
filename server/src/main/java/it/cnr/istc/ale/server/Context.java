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
package it.cnr.istc.ale.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.istc.ale.api.Lesson;
import it.cnr.istc.ale.api.Parameter;
import it.cnr.istc.ale.api.messages.NewStudent;
import it.cnr.istc.ale.api.messages.LostStudent;
import it.cnr.istc.ale.api.messages.LostParameter;
import it.cnr.istc.ale.api.messages.Message;
import it.cnr.istc.ale.api.messages.NewLesson;
import it.cnr.istc.ale.api.messages.NewParameter;
import it.cnr.istc.ale.api.model.LessonModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Context {

    private static final Logger LOG = Logger.getLogger(Context.class.getName());
    public static final String SERVER_ID = "server";
    private static Context context;
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static Context getContext() {
        if (context == null) {
            context = new Context();
        }
        return context;
    }
    private MqttClient mqtt;
    private final Map<Long, Map<String, Parameter>> parameter_types = new HashMap<>();
    private final Map<Long, Map<String, Map<String, String>>> parameter_values = new HashMap<>();
    private final Map<Lesson, LessonModel> models = new IdentityHashMap<>();
    private final Map<Long, Collection<Lesson>> lessons = new HashMap<>();
    private final Map<Long, Collection<Lesson>> followed_lessons = new HashMap<>();

    private Context() {
        try {
            mqtt = new MqttClient("tcp://" + Config.getInstance().getParam(Config.Param.Host) + ":" + Config.getInstance().getParam(Config.Param.MQTTPort), SERVER_ID, new MemoryPersistence());
            mqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    LOG.log(Level.SEVERE, null, cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(true);
            mqtt.connect(options);
        } catch (MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void addConnection(long id) {
        if (!parameter_types.containsKey(id)) {
            parameter_types.put(id, new HashMap<>());
        }
        if (!parameter_values.containsKey(id)) {
            parameter_values.put(id, new HashMap<>());
        }
        try {
            mqtt.publish(id + "/output/on-line", Boolean.TRUE.toString().getBytes(), 1, true);
            mqtt.subscribe(id + "/output", (String topic, MqttMessage message) -> {
                Message m = MAPPER.readValue(message.getPayload(), Message.class);
                if (m instanceof NewParameter) {
                    NewParameter np = (NewParameter) m;
                    Parameter par = np.getParameter();
                    parameter_types.get(id).put(par.getName(), par);
                    mqtt.subscribe(id + "/output/" + par.getName(), (String par_topic, MqttMessage par_value) -> {
                        parameter_values.get(id).put(par.getName(), MAPPER.readValue(par_value.getPayload(), new TypeReference<Map<String, String>>() {
                        }));
                    });
                } else if (m instanceof LostParameter) {
                    LostParameter lp = (LostParameter) m;
                    mqtt.unsubscribe(id + "/output/" + lp.getName());
                    parameter_types.get(id).remove(lp.getName());
                    parameter_values.get(id).remove(lp.getName());
                }
            });
        } catch (MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void removeConnection(long id) {
        try {
            for (Map.Entry<String, Parameter> par_type : parameter_types.get(id).entrySet()) {
                mqtt.unsubscribe(id + "/output/" + par_type.getKey());
            }
            mqtt.unsubscribe(id + "/output");
            mqtt.publish(id + "/output/on-line", Boolean.FALSE.toString().getBytes(), 1, true);
        } catch (MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void add_teacher(long student_id, long teacher_id) {
        try {
            mqtt.publish(teacher_id + "/input", MAPPER.writeValueAsBytes(new NewStudent(student_id)), 1, false);
        } catch (JsonProcessingException | MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void remove_teacher(long student_id, long teacher_id) {
        try {
            mqtt.publish(teacher_id + "/input", MAPPER.writeValueAsBytes(new LostStudent(student_id)), 1, false);
        } catch (JsonProcessingException | MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public Map<String, Parameter> get_parameter_types(long user_id) {
        return parameter_types.get(user_id);
    }

    public boolean is_online(long user_id) {
        return parameter_types.containsKey(user_id);
    }

    public Lesson new_lesson(long teacher_id, String name, LessonModel model, Map<String, Long> roles) {
        Lesson l = new Lesson(teacher_id, name, roles);
        models.put(l, model);
        if (!lessons.containsKey(teacher_id)) {
            lessons.put(teacher_id, new ArrayList<>());
        }
        lessons.get(teacher_id).add(l);
        for (Long student_id : roles.values()) {
            if (!followed_lessons.containsKey(student_id)) {
                followed_lessons.put(student_id, new ArrayList<>());
            }
            followed_lessons.get(student_id).add(l);
            try {
                mqtt.publish(student_id + "/input", MAPPER.writeValueAsBytes(new NewLesson(l)), 1, false);
            } catch (JsonProcessingException | MqttException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }

    public Collection<Lesson> get_lessons(long teacher_id) {
        return lessons.containsKey(teacher_id) ? lessons.get(teacher_id) : Collections.emptyList();
    }

    public Collection<Lesson> get_followed_lessons(long student_id) {
        return followed_lessons.containsKey(student_id) ? followed_lessons.get(student_id) : Collections.emptyList();
    }
}
