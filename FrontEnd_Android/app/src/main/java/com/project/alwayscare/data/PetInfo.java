package com.project.alwayscare.data;

import android.os.Parcelable;

import java.io.Serializable;

public class PetInfo implements Serializable {
    private final long petId;
    private final String name;
    private final int age;
    private final String imageUri;
    private final String species;
    private final int type;

    public PetInfo(long petId, String name, int age, String imageUri, String species, int type) {
        this.petId = petId;
        this.name = name;
        this.age = age;
        this.imageUri = imageUri;
        this.species = species;
        this.type = type;
    }

    public long getPetId() { return petId; }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getSpecies() {
        return species;
    }

    public int getType() { return type; }
}
