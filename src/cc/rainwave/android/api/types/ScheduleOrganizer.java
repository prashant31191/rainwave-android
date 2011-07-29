package cc.rainwave.android.api.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ScheduleOrganizer implements Parcelable {
    private Event mCurrent;
    
    private Event mHistory[], mNext[];
    
    private ScheduleOrganizer(Parcel source) {
        mCurrent = source.readParcelable(Event.class.getClassLoader());
        
        Parcelable history[] = source.readParcelableArray(Event.class.getClassLoader());
        Parcelable next[] = source.readParcelableArray(Event.class.getClassLoader());
        
        mHistory = new Event[history.length];
        mNext = new Event[next.length];
        
        for(int i = 0; i < history.length; i++) { mHistory[i] = (Event) history[i]; }
        for(int i = 0; i < next.length; i++) { mNext[i] = (Event) next[i]; }
    }
    
    public ScheduleOrganizer() { }

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
                
                // This little doodad just finds the first member inside the
                // object and extracts its name and the data it is bound to.
                Iterator<Entry<String, JsonElement>> it = members.iterator();
                Entry<String,JsonElement> first = it.next();
                String name = first.getKey();
                JsonElement data = first.getValue();
                
                // Hopefully there will only ever be three types of schedules,
                // so we'll just check the name of the member and assign it to
                // the appropriate field.
                
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mCurrent, flags);
    }
    
    public static final Parcelable.Creator<ScheduleOrganizer> CREATOR
    = new Parcelable.Creator<ScheduleOrganizer>() {
        @Override
        public ScheduleOrganizer createFromParcel(Parcel source) {
            return new ScheduleOrganizer(source);
        }

        @Override
        public ScheduleOrganizer[] newArray(int size) {
            return new ScheduleOrganizer[size];
        }
    };
}
