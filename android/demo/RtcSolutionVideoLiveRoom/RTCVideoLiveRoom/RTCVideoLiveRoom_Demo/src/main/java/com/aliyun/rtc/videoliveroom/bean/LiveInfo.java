package com.aliyun.rtc.videoliveroom.bean;


public class LiveInfo {

    private String channelId;
    private String ownerId;
    private String coverUrl;
    private String title;
    private CreateDateTimeBean createDateTime;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CreateDateTimeBean getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(CreateDateTimeBean createDateTime) {
        this.createDateTime = createDateTime;
    }

    public static class CreateDateTimeBean {

        private int dayOfMonth;
        private String dayOfWeek;
        private int dayOfYear;
        private int hour;
        private int minute;
        private int second;
        private int nano;
        private int year;
        private String month;
        private int monthValue;
        private ChronologyBean chronology;

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public void setDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public int getDayOfYear() {
            return dayOfYear;
        }

        public void setDayOfYear(int dayOfYear) {
            this.dayOfYear = dayOfYear;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public int getNano() {
            return nano;
        }

        public void setNano(int nano) {
            this.nano = nano;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public int getMonthValue() {
            return monthValue;
        }

        public void setMonthValue(int monthValue) {
            this.monthValue = monthValue;
        }

        public ChronologyBean getChronology() {
            return chronology;
        }

        public void setChronology(ChronologyBean chronology) {
            this.chronology = chronology;
        }

        public static class ChronologyBean {
            /**
             * id : ISO
             * calendarType : iso8601
             */

            private String id;
            private String calendarType;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getCalendarType() {
                return calendarType;
            }

            public void setCalendarType(String calendarType) {
                this.calendarType = calendarType;
            }
        }
    }

}
