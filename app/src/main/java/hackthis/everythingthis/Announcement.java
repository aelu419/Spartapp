package hackthis.everythingthis;

class Club {
    private CharSequence name;
    private String manager /*like the student or teacher that manages the club*/;

    public Club(CharSequence name, String manager) {
        this.setName(name);
        this.setManager(manager);
    }
    /**
     * there's a tab in the database with clubs we can implement or something
     * @return
     */

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
}

class Time {
    String formatted = "";

    public Time(int unformatted) {
        String unformattedString = "" + unformatted;
        formatted += unformattedString.substring(6, 8) + " "; // day
        int month = Integer.parseInt(unformattedString.substring(4, 6));
        switch (month) { // changes the number to the month name (this isn't all that useful but it might
            // come in handy)
            case 1:
                formatted += "January ";
                break;
            case 2:
                formatted += "February ";
                break;
            case 3:
                formatted += "March ";
                break;
            case 4:
                formatted += "April ";
                break;
            case 5:
                formatted += "May ";
                break;
            case 6:
                formatted += "June ";
                break;
            case 7:
                formatted += "July ";
                break;
            case 8:
                formatted += "August ";
                break;
            case 9:
                formatted += "September ";
                break;
            case 10:
                formatted += "October ";
                break;
            case 11:
                formatted += "November ";
                break;
            case 12:
                formatted += "December ";
                break;
            default:
                formatted += " ";
        }

        formatted += unformattedString.substring(0, 4); // the year
    }

    public void printFormatted() {
        System.out.println(formatted);
    }

    public String getFormatted() {
        return formatted;
    }
}

public class Announcement {
    private CharSequence announcement;
    private Time postTime; //see above for the class
    private Club club; //see above for the class

    public Announcement(CharSequence announcement /* What does it say? */, Time postTime /* When was it posted? */, Club club /*Who made the announcement?*/) {
        this.setAnnouncement(announcement);
        this.setPostTime(postTime);
        this.setClub(club);
    }
    /**
     * maybe we could use rss; it doesn't really simplify things, it just makes it easier to post
     * maybe we could add a subscribe option to only get messages from clubs you care about
     * @return
     */

    public CharSequence getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(CharSequence announcement) {
        this.announcement = announcement;
    }

    /**
     * I used CharSequence because by default the text in a textview is a
     * CharSequence Strings could just cast themselves to CharSequence
     *
     * @return
     */

    public Club getClub() {
        return this.club;
    }

    public void setClub(Club c) {
        this.club = c;
    }
    /**
     * somewhat work in progress at the moment
     * @return
     */

    public Time getPostTime() {
        return postTime;
    }

    /**
     * add a thing to calculate how long ago such as 197001011200 (jan 1 1970 at
     * 12pm) returning 48 years try to retain this format if possible
     *
     * @param postTime
     */

    public void setPostTime(Time postTime) {
        this.postTime = postTime;
    }

    public String convertToString(/* converts the announcement to a string for other use if needed */) {
        return this.announcement.toString();
    }

    public CharSequence convertFromString(String str) {
        CharSequence chars = "" + str;
        return chars;
    }

    /**
     * <h1>Example:</h1>
     * Club ArbitraryClub = new Club("ArbitraryClubName", "Bob Ross");
     * Announcement A = new Announcement("Cats", 19700101 (jan 1 1970), ArbitraryClub)
     * A.getAnnouncement would return Cats
     * A.getPostTime would return 19700101
     * A.getPostTime.formatted would return 01 January 1970 (if that's ever useful)
     * A.getClub would return the arbitrary club
     *
     * We could use this to make announcements and get the text or something and make an arraylist of announcements to put into the app
     *
     * We can do something like...
     * TextView t = (TextView)findViewById(R.id.text_view);
     * Announcement a = new Announcements("Cats", 19700101, ArbitraryClub);
     * t.setText(a.getText);
     * ...to set the text
     *
     * We can also somehow get announcements from the database this way
     */
}
