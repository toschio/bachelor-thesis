package com.edutec.moodleXapiTransformer.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class TombStone {

    private boolean tombstone = false;


    public TombStone(boolean isTombostone) {
        this.tombstone = isTombostone;
    }

    public static <T extends TombStone> T dig(T object) {
        object.setTombstone(true);
        return object;
    }
}
