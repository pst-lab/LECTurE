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
package it.cnr.istc.ale.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Riccardo De Benedictis
 */
public class URLEvent extends Event {

    private final String content;
    private final String url;

    @JsonCreator
    public URLEvent(@JsonProperty("lessonId") long lesson_id, @JsonProperty("id") int id, @JsonProperty("content") String content, @JsonProperty("url") String url) {
        super(lesson_id, id);
        this.content = content;
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }
}