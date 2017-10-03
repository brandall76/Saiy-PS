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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

import static ai.saiy.android.api.request.SaiyRequestParams.SILENCE;
import static ai.saiy.android.tts.SaiyTextToSpeech.ARRAY_DELIMITER;
import static ai.saiy.android.tts.SaiyTextToSpeech.ARRAY_FIRST;
import static ai.saiy.android.tts.SaiyTextToSpeech.ARRAY_INTERIM;
import static ai.saiy.android.tts.SaiyTextToSpeech.ARRAY_LAST;
import static ai.saiy.android.tts.SaiyTextToSpeech.ARRAY_SINGLE;

/**
 * Created by benrandall76@gmail.com on 12/09/2016.
 */

public class SoundEffectHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SoundEffectHelper.class.getSimpleName();

    public static final Pattern pSOUND_EFFECT = Pattern.compile(".*\\[[0-9A-Za-z\\s]+\\].*", Pattern.DOTALL);
    private static final Pattern pSOUND_EFFECT_CONTENT = Pattern.compile("(.*?)\\[(.*?)\\]", Pattern.DOTALL);

    private static final int REMOVE = 0;
    private static final int SAY = 1;
    private static final int SOUND = 2;

    private final CharSequence text;
    private final String utteranceId;
    private final Gender gender;

    private final ArrayList<SoundEffectItem> itemArray = new ArrayList<>();

    public static ArrayList<String> addedItems = new ArrayList<>();

    /**
     * Constructor
     *
     * @param text the utterance to extract speech and sound effects from
     */
    public SoundEffectHelper(@NonNull final CharSequence text, @NonNull final String utteranceId,
                             @NonNull final Gender gender) {
        this.text = text;
        this.utteranceId = utteranceId;
        this.gender = gender;
    }

    /**
     * Get the sorted speech and sound results
     *
     * @return the resolved {@link ArrayList} containing ordered {@link SoundEffectItem} objects
     */
    public ArrayList<SoundEffectItem> getArray() {
        getValidatedArray();
        return checkSingle();
    }

    /**
     * Check that the array doesn't finally, only contain a single entry. If it does, the
     * utterance id needs to be manipulated to avoid it failing TTS initialisation checks.
     *
     * @return the resolved {@link ArrayList} containing ordered {@link SoundEffectItem} objects
     */
    public ArrayList<SoundEffectItem> checkSingle() {

        if (itemArray.size() == 1) {
            itemArray.get(0).setUtteranceId(ARRAY_SINGLE + ARRAY_DELIMITER + utteranceId);
        }

        return itemArray;
    }

    private ArrayList<SoundEffectItem> getValidatedArray() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getValidatedArray: " + itemArray.size());
        }

        if (UtilsList.notNaked(itemArray)) {

            final ListIterator<SoundEffectItem> itr = itemArray.listIterator(itemArray.size() - 1);

            if (itr.hasNext()) {
                final SoundEffectItem item = itr.next();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getValidatedArray: getText: " + item.getText());
                    MyLog.i(CLS_NAME, "getValidatedArray: getUtteranceId: " + item.getUtteranceId());
                    MyLog.i(CLS_NAME, "getValidatedArray: soundEffect: " + item.getItemType());
                }

                switch (item.getItemType()) {

                    case SoundEffectItem.SILENCE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getValidatedArray: removing silence");
                        }
                        itr.remove();
                        return getValidatedArray();
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getValidatedArray: updating to ARRAY_LAST");
                        }
                        item.setUtteranceId(ARRAY_LAST + ARRAY_DELIMITER + utteranceId);
                        break;
                }
            }
        }

        return itemArray;
    }

    /**
     * Begin sorting the utterance
     */
    public void sort() {
        extract(this.text.toString());
    }

    /**
     * Method to loop over a gradually reducing utterance.
     *
     * @param text the utterance
     */
    private void extract(@Nullable final String text) {

        if (UtilsString.notNaked(text)) {

            String nextToCheck;

            try {

                final Matcher matcher = pSOUND_EFFECT_CONTENT.matcher(text);

                SoundEffectItem item;
                String remove;
                String soundEffect;
                String say;

                if (matcher.find()) {

                    remove = matcher.group(REMOVE);
                    say = matcher.group(SAY);

                    if (UtilsString.notNaked(say)) {

                        item = new SoundEffectItem(UtilsString.stripLeadingPunctuation(say.trim()),
                                SoundEffectItem.SPEECH, resolveUtteranceId());
                        itemArray.add(item);
                    }

                    soundEffect = matcher.group(SOUND);
                    nextToCheck = text.replaceFirst(Pattern.quote(remove), "");

                    if (UtilsString.notNaked(soundEffect)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getSoundEffect: " + soundEffect.trim() + " ~ " + gender.name());
                        }

                        item = new SoundEffectItem(SoundEffect.getSoundEffect(soundEffect.trim(), gender),
                                SoundEffectItem.SOUND,
                                UtilsString.notNaked(nextToCheck) ? resolveUtteranceId()
                                        : ARRAY_LAST + ARRAY_DELIMITER + this.utteranceId);
                        itemArray.add(item);

                        if (UtilsString.notNaked(nextToCheck)
                                && UtilsString.notNaked(
                                UtilsString.stripLeadingPunctuation(nextToCheck.trim()))) {

                            item = new SoundEffectItem(SILENCE, SoundEffectItem.SILENCE,
                                    ARRAY_INTERIM + ARRAY_DELIMITER + this.utteranceId);
                            itemArray.add(item);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "extract: ignoring remaining punctuation");
                            }

                            nextToCheck = null;
                        }
                    }

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "extract: remove: " + remove);
                        MyLog.i(CLS_NAME, "extract: say: " + say);
                        MyLog.i(CLS_NAME, "extract: soundEffect: " + soundEffect);
                        MyLog.i(CLS_NAME, "extract: nextToCheck: " + nextToCheck);
                    }
                } else {

                    if (UtilsString.notNaked(text)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "extract: no more sounds: " + text);
                        }

                        if (UtilsString.notNaked(UtilsString.stripLeadingPunctuation(text.trim()))) {

                            item = new SoundEffectItem(UtilsString.stripLeadingPunctuation(text.trim()),
                                    SoundEffectItem.SPEECH, ARRAY_LAST + ARRAY_DELIMITER + this.utteranceId);
                            itemArray.add(item);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "extract: ignoring remaining punctuation");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "extract: ignoring remaining white space");
                        }
                    }

                    nextToCheck = null;
                }

            } catch (final ArrayIndexOutOfBoundsException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "extract: ArrayIndexOutOfBoundsException" + text);
                    e.printStackTrace();
                }
                nextToCheck = null;
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "extract: NullPointerException" + text);
                    e.printStackTrace();
                }
                nextToCheck = null;
            }

            if (UtilsString.notNaked(nextToCheck)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "extract: continuing with: " + nextToCheck);
                }
                extract(nextToCheck.trim());
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "extract: complete");
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "extract: text naked: complete");
            }
        }
    }

    private String resolveUtteranceId() {
        return itemArray.isEmpty() ? ARRAY_FIRST + ARRAY_DELIMITER + utteranceId
                : ARRAY_INTERIM + ARRAY_DELIMITER + utteranceId;
    }

    /**
     * Store all sound effect names that have been added to the engine.
     *
     * @param updatedAddedItems the list of sound effect names
     */
    public static void setAddedItems(@NonNull final ArrayList<String> updatedAddedItems) {
        addedItems = updatedAddedItems;
    }

    /**
     * Get the list of sound effect names that have been added to the engine
     *
     * @return the {@link ArrayList} of sound effect names
     */
    public static ArrayList<String> getAddedItems() {
        return addedItems;
    }
}
