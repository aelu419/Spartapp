package hackthis.everythingthis;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by HP on 2018/4/30.
 */

public class Subject{

    protected final String name;
    protected final String teacher;
    protected final String room;
    protected final String type;
    protected final int colorInt;
    protected final int imageInt;

    private Context context;

    public static ArrayList<String> subjectTypes;
    public static ArrayList<String[]> allSubjects;

    static{
        subjectTypes = new ArrayList<>(30);
        allSubjects = new ArrayList<>(30);
        subjectTypes.add("art");
        allSubjects.add(new String[]{"ceramics", "easternart6", "easternart7", "easternart8", "easternarti", "easternartii", "foundationsofart", "foundationsofdigitalart", "introtosculpture", "paintingi", "paintingii", "paintingiii", "theartportfolio", "westernart6", "westernart7", "westernart8"});
        subjectTypes.add("beginningguitar");
        allSubjects.add(new String[]{"beginningguitar"});
        subjectTypes.add("biology");
        allSubjects.add(new String[]{"biology"});
        subjectTypes.add("chinese");
        allSubjects.add(new String[]{"apchinese", "chinese6a", "chinese6b", "chinese7a", "chinese7b", "chinese8a", "chinese8b", "chinese9a", "chinese9b", "chinese10a", "chinese10b", "chinese11a", "chinese11b", "chinese12a", "chinese12b"});
        subjectTypes.add("choir");
        allSubjects.add(new String[]{"choir"});
        subjectTypes.add("computer");
        allSubjects.add(new String[]{"apcomputersciencea", "desktoppublishing", "digitalphotography", "digitalvideo", "grade6computerfoundations", "grade7computerfoundations", "grade8computerfoundations", "introtocomputerscience", "mobileappdesign", "roboticsi", "roboticsii", "webdesign"});
        subjectTypes.add("filmstudies");
        allSubjects.add(new String[]{"filmstudyi", "filmstudyii"});
        subjectTypes.add("french");
        allSubjects.add(new String[]{"frenchi", "frenchii", "frenchiii", "frenchiv"});
        subjectTypes.add("highschoolenrichment");
        allSubjects.add(new String[]{"highschoolenrichment"});
        subjectTypes.add("linguistics");
        allSubjects.add(new String[]{"linguistics"});
        subjectTypes.add("math");
        allSubjects.add(new String[]{"advalgebraii", "advgeometry", "algebrai", "algebraii", "algebraiii", "algebraii/trignometry", "apcalculusab", "apcalculusbc","apstatistics", "appliedmath","calculus","geometry", "introtolinearalgebra", "math6", "math7", "precalculus"});
        subjectTypes.add("music");
        allSubjects.add(new String[]{"advband8", "band", "beginninginstrumentalmusic", "instrumentalmusicband6", "instrumentalmusicband7", "vocalmusic6", "vocalmusic7"});
        subjectTypes.add("spanish");
        allSubjects.add(new String[]{"spanishi", "spanishii", "spanishiii", "spanishiv", "bilingualtranslation"});
        subjectTypes.add("steam");
        allSubjects.add(new String[]{"steam", "steami", "steamii", "steamiii", "steamday"});
        subjectTypes.add("chemistry");
        allSubjects.add(new String[]{"apchemistry", "chemistry"});
        subjectTypes.add("economics");
        allSubjects.add(new String[]{"apmacroeconomics", "economics"});
        subjectTypes.add("english");
        allSubjects.add(new String[]{"langarts6", "langarts7", "langarts8", "englih9", "english10", "englih11", "englih12", "apenglishlanguage&composition", "aplang"});
        subjectTypes.add("history");
        allSubjects.add(new String[]{"ancientworldhistory7", "apushistory", "apworldhistory", "arthistorymethods", "chinesehistory6", "chinesehistory7", "chinesehistory8", "chinesehistoryi", "chinesehistoryii", "Geography 6","medievalworldhistory8", "modernworldhistory", "ushistory",});
        subjectTypes.add("socialstudy");
        allSubjects.add(new String[]{"currentaffairs", "digitalethnography", "foundationsofmodernchina", "humanities", "philosophy"});
        subjectTypes.add("physics");
        allSubjects.add(new String[]{"apphysicsi", "apphysicsii", "earthandspacescience", "earthscience6", "lifescience7", "physicalscience8"});
        subjectTypes.add("piano");
        allSubjects.add(new String[]{"pianoi", "pianoii"});
        subjectTypes.add("studyhall");
        allSubjects.add(new String[]{"studyhall"});
        subjectTypes.add("theater");
        allSubjects.add(new String[]{"advacting", "classicalacting", "theater6", "theater7", "theater8"});
        subjectTypes.add("genderstudies");
        allSubjects.add(new String[]{"genderstudiesi"});
        subjectTypes.add("els");
        allSubjects.add(new String[]{"langsupport6", "langsupport7", "langsupport8", "langsupport9", "langsupport10"});
        subjectTypes.add("health");
        allSubjects.add(new String[]{"health7", "health8", "health9", "health10"});
        subjectTypes.add("fitness");
        allSubjects.add(new String[]{"advfitness"});
        subjectTypes.add("sport");
        allSubjects.add(new String[]{"outdooreducation", "pe6", "pe7", "pe8", "pe9", "sportsmanagement", "strengthtraining", "ultimatesports"});
    }


    public Subject( String name, String teacher, String room, Context context){
        this.name = name;
        this.teacher = teacher;
        this.room = room;
        this.type = searchSubjectType(trimName(name.toLowerCase()));
        this.context = context;
        colorInt = getColor(type);
        imageInt = findDrawableWithString(type);
    }

    public int getColor( String subjectName ){
        return context.getResources().getIdentifier(subjectName, "color", context.getPackageName());
    }

    public int findDrawableWithString(String source){
        Log.d("coursefind",source);
        int id = context.getResources().getIdentifier("course_"+source, "drawable", context.getPackageName());
        return id;
    }

    public String name(){return name;}
    public String teacher(){return teacher;}
    public String room(){return room;}
    public String type(){return type;}

    public boolean equals(Object obj){
        Subject temp = (Subject)obj;
        return (temp.name().equals(this.name));
    }

    public String searchSubjectType(String name){
        Log.d("Demo","-binsearch for "+name);
        for(int i = 0; i < allSubjects.size(); i++){
            if(name.contains(subjectTypes.get(i))){
                return subjectTypes.get(i);
            }
            for(int j = 0; j < allSubjects.get(i).length; j++){
                if(name.contains(allSubjects.get(i)[j])){
                    return subjectTypes.get(i);
                }
            }
        }
        Log.d("coursefind",name +" returned with type none");
        return "none";
    }

    public static String trimName(String str){
        ArrayList<Character> temp = new ArrayList<>(20);
        Log.d("trim",str);
        char[] charStr = str.toCharArray();
        for(int i = 0; i < charStr.length; i++){
            int ascii = (int)charStr[i];
            if((ascii <= 90 && ascii >=65) || (ascii <= 122 && ascii >= 97) || (ascii <= 57 && ascii >= 48)){
                temp.add(charStr[i]);
            }
        }
        char[] charTemp = new char[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            charTemp[i] = temp.get(i);
        }
        Log.d("trim","\tfinished with"+new String(charTemp));
        return new String(charTemp);
    }

    public String toString(){return name;}
}
