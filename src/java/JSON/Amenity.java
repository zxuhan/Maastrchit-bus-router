package src.java.JSON;

public enum Amenity {
    HOSPITAL(100),
    POLICE(95),
    FIRE_STATION(90),
    SCHOOL(85),
    UNIVERSITY(80),
    DOCTORS(75),
    PHARMACY(70),
    CLINIC(65),
    SOCIAL_FACILITY(60),
    CHILDCARE(55),
    NURSING_HOME(50),
    FUEL(45),
    ATM(40),
    BANK(40),
    PLACE_OF_WORSHIP(40),
    COURTHOUSE(35),
    LIBRARY(35),
    COMMUNITY_CENTRE(30),
    POST_OFFICE(30),
    RESTAURANT(30),
    CAFE(30),
    FAST_FOOD(30),
    SUPERMARKET(30),
    CINEMA(25),
    THEATRE(25),
    ARTS_CENTRE(25),
    BAR(20),
    PUB(20),
    NIGHTCLUB(20),
    MARKETPLACE(20),
    TAXI(20),
    PARKING(20),
    CHARGING_STATION(20),
    BICYCLE_RENTAL(20),
    CAR_RENTAL(20),
    BICYCLE_PARKING(15),
    PARKING_ENTRANCE(15),
    PARKING_SPACE(15),
    WATER_POINT(15),
    DRINKING_WATER(15),
    TOILETS(15),
    BENCH(10),
    RECYCLING(10),
    WASTE_BASKET(10),
    VENDING_MACHINE(10),
    FOUNTAIN(10),
    ICE_CREAM(10),
    PHOTO_BOOTH(10),
    LUGGAGE_LOCKER(10),
    INFORMATION(10),
    CLOCK(10),
    PUBLIC_BOOKCASE(10),
    BINOCULARS(10),
    SHELTER(5),
    SHOWER(5),
    BROTHEL(5),
    RESTHOUSE(5),
    PREP_SCHOOL(5),
    CAR_WASH(5),
    BUREAU_DE_CHANGE(5),
    MOPED_PARKING(5),
    HUNTING_STAND(5);

    private final int points;

    Amenity(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public static Amenity fromString(String amenityName) {
        try {
            return Amenity.valueOf(amenityName.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
