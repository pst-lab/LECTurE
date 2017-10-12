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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "message-type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Answer.class, name = "answer")
    ,@JsonSubTypes.Type(value = Event.class, name = "event")
    ,@JsonSubTypes.Type(value = EventUpdate.class, name = "event-update")
    ,@JsonSubTypes.Type(value = NewConnection.class, name = "new-connection")
    ,@JsonSubTypes.Type(value = NewEvent.class, name = "new-event")
    ,@JsonSubTypes.Type(value = NominalUpdate.class, name = "nominal-update")
    ,@JsonSubTypes.Type(value = NumericUpdate.class, name = "numeric-update")})
public abstract class Message {
}
