package org.example.common.network;

import org.example.common.models.StudyGroup;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class Response implements Serializable {
    private final StatusCode status;
    private String response = "";
    private Collection<StudyGroup> collection;

    public Response(StatusCode status) {
        this.status = status;
    }

    public Response(StatusCode status, String response) {
        this.status = status;
        this.response = response.trim();
    }

    public Response(StatusCode status, String response, Collection<StudyGroup> collection) {
        this.status = status;
        this.response = response.trim();
        this.collection = collection.stream()
                .sorted(Comparator.comparing(StudyGroup::getId))
                .toList();
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getResponse() {
        return response;
    }

    public Collection<StudyGroup> getCollection() {
        return collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response response1)) return false;
        return status == response1.status && Objects.equals(response, response1.response) && Objects.equals(collection, response1.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, response, collection);
    }

    @Override
    public String toString(){
        return "Response[" + status +
                (response.isEmpty()
                        ? ""
                        :',' + response) +
                (collection == null
                        ? ']'
                        : ',' + collection.toString() + ']');
    }
}