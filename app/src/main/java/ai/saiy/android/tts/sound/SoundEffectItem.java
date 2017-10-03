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

package ai.saiy.android.tts.sound;

/**
 * Created by benrandall76@gmail.com on 12/09/2016.
 */

public class SoundEffectItem {

    public static final int SPEECH = 0;
    public static final int SOUND = 1;
    public static final int SILENCE = 2;

    private String text;
    private int itemType;
    private String utteranceId;

    /**
     * Constructor
     *
     * @param text        the utterance or sound effect identifier
     * @param itemType    one of {@link #SOUND} or {@link #SPEECH}
     * @param utteranceId the id
     */
    public SoundEffectItem(final String text, final int itemType, final String utteranceId) {
        this.text = text;
        this.itemType = itemType;
        this.utteranceId = utteranceId;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(final int itemType) {
        this.itemType = itemType;
    }

    public String getUtteranceId() {
        return utteranceId;
    }

    public void setUtteranceId(final String utteranceId) {
        this.utteranceId = utteranceId;
    }
}
