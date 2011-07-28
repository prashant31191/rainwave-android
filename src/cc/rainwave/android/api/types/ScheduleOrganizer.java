package cc.rainwave.android.api.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import android.util.Log;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ScheduleOrganizer {
    private Event mCurrent;
    
    private Event mHistory[], mNext[];
    
    public Song getCurrentSong() {
        return mCurrent.song_data[0];
    }
    
    public static class Deserializer implements JsonDeserializer<ScheduleOrganizer> {
        private static final String TAG = "ScheduleOrganizer.Deserializer";

        @Override
        public ScheduleOrganizer deserialize(JsonElement json, Type type,
                JsonDeserializationContext ctx) throws JsonParseException {
            JsonArray mainArray = json.getAsJsonArray();
            
            ScheduleOrganizer organizer = new ScheduleOrganizer();
            
            // mainArray should be an array of JsonObjects
            for(int i = 0; i < mainArray.size(); i++) {
                JsonElement scheduleJson = mainArray.get(i);
                JsonObject schedule = scheduleJson.getAsJsonObject();
                
                // The schedule as a JSON should contain one member which is bound
                // to the actual schedule data.
                Set<Map.Entry<String, JsonElement>> members = schedule.entrySet();
                
                Log.d(TAG, "Number of members: " + members.size());

                // This little doodad just finds the first member inside the
                // object and extracts its name and the data it is bound to.
                Iterator<Entry<String, JsonElement>> it = members.iterator();
                Entry<String,JsonElement> first = it.next();
                String name = first.getKey();
                JsonElement data = first.getValue();
                
                // 'data' is now an Event object, or an array of them.
                // We're going to assume that if 'data' is a JsonObject, that
                // it is the current event 'sched_current'. If it is a JsonArray, we'll stuff
                // it in mSchedules for later.
                
                // TODO: WARNING! WARNING! WARNING!
                // This might be a bit dangerous in the future if somebody
                // decides that something other
                
                if(name.compareTo(SCHED_HISTORY) == 0) {
                    organizer.mHistory = (Event[]) ctx.deserialize(data, Event[].class);
                }
                else if(name.compareTo(SCHED_NEXT) == 0) {
                    organizer.mNext = (Event[]) ctx.deserialize(data, Event[].class);
                }
                else {
                    organizer.mCurrent = ctx.deserialize(data, Event.class);
                }
            }
            
            return organizer;
        }
        
        public static final String
            SCHED_CURRENT = "sched_current",
            SCHED_NEXT = "sched_next",
            SCHED_HISTORY = "sched_history";
    }
}