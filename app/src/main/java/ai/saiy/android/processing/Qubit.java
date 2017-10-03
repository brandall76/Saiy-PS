/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.processing;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.defaults.songrecognition.SongRecognitionChooser;

/**
 * Created by benrandall76@gmail.com on 13/06/2016.
 */
public class Qubit {

    private String clipboardContent;
    private String spellContent;
    private String translatedText;
    private ArrayList<SongRecognitionChooser> songRecognitionChooserList;

    public ArrayList<SongRecognitionChooser> getSongRecognitionChooserList() {
        return songRecognitionChooserList;
    }

    public void setSongRecognitionChooserList(@NonNull final ArrayList<SongRecognitionChooser> songRecognitionChooserList) {
        this.songRecognitionChooserList = songRecognitionChooserList;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(@NonNull final String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSpellContent() {
        return spellContent;
    }

    public void setSpellContent(@NonNull final String spellContent) {
        this.spellContent = spellContent;
    }

    public String getClipboardContent() {
        return clipboardContent;
    }

    public void setClipboardContent(@NonNull final String clipboardContent) {
        this.clipboardContent = clipboardContent;
    }
}
