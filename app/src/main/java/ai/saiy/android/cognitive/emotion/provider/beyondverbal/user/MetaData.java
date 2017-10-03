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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.user;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to set additional optional information regarding the user. Such parameters will have future
 * uses, such as direct and social media sharing.
 * <p>
 * In the case of encrypted emotion analysis, the unique device id will need to be set.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class MetaData {

    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String DEVICE_ID = "deviceId";
    private static final String FACEBOOK_ID = "facebookId";
    private static final String TWITTER_ID = "twitterId";

    private static final String DEFAULT_DEVICE_ID = "default_device_id";
    private static final String DEFAULT_DEVICE_PHONE = "default_phone";
    private static final String DEFAULT_DEVICE_EMAIL = "default_email";
    private static final String DEFAULT_DEVICE_FACEBOOK_ID = "default_facebook_id";
    private static final String DEFAULT_DEVICE_TWITTER_HANDLE = "default_twitter_id";

    private String email;
    private String phone;
    private String deviceId;
    private String facebookId;
    private String twitterId;

    /**
     * Default Constructor
     */
    public MetaData() {
    }

    /**
     * Constructor
     *
     * @param deviceId   the device unique id
     * @param email      the user's email address
     * @param facebookId the user's facebook url
     * @param phone      the user's phone number
     * @param twitterId  the user's Twitter handle
     */
    public MetaData(@Nullable final String deviceId, @Nullable final String email,
                    @Nullable final String facebookId, @Nullable final String phone,
                    @Nullable final String twitterId) {
        this.deviceId = deviceId;
        this.email = email;
        this.facebookId = facebookId;
        this.phone = phone;
        this.twitterId = twitterId;
    }

    public static MetaData getEmpty() {
        return new MetaData(DEFAULT_DEVICE_ID, DEFAULT_DEVICE_EMAIL,
                DEFAULT_DEVICE_FACEBOOK_ID, DEFAULT_DEVICE_PHONE, DEFAULT_DEVICE_TWITTER_HANDLE);
    }

    /**
     * Method to prepare the meta data in a JSON format that the API will accept.
     *
     * @return a JSON formatted representation of the meta data
     */
    public JSONObject getMetaJSON() {

        final JSONObject object = new JSONObject();

        try {
            object.put(EMAIL, getEmail());
            object.put(PHONE, getPhone());
            object.put(DEVICE_ID, getDeviceId());
            object.put(FACEBOOK_ID, getFacebookId());
            object.put(TWITTER_ID, getTwitterId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@NonNull final String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull final String email) {
        this.email = email;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(@NonNull final String facebookId) {
        this.facebookId = facebookId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(@NonNull final String phone) {
        this.phone = phone;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(@NonNull final String twitterId) {
        this.twitterId = twitterId;
    }
}

