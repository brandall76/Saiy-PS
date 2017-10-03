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

package ai.saiy.android.personality;

/**
 * The more features of the application that are interacted with, the more the 'AI Value' increases,
 * which is shown to the user in the permanent notification.
 * <p>
 * The AI Level is based on very little that is Artificially Intelligent....
 * <p>
 * Created by benrandall76@gmail.com on 22/03/2016.
 */
public class AI {

    // Placeholder value
    public static double AI_LEVEL = 0.25;

    /**
     * Get the current AI Level to display
     *
     * @return the AI Level
     */
    public static String getAILevel() {
        return String.valueOf(calculateAI());
    }

    /**
     * Check how much of the application's functionality the user has used and is using. More = higher.
     *
     * @return the AI Level
     */
    private static double calculateAI() {
        // TODO
        return AI_LEVEL;
    }
}
