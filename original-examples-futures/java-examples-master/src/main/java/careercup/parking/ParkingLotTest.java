package careercup.parking;

public class ParkingLotTest {

    public static void main(String[] args) throws Exception {
        ParkingLot parking = new ParkingLot();
        Vehicle car = new Car();
        Vehicle motorcycle = new MotorCycle();

        for (int i = 0; i < 26; i++) {
            Slot tokenNumber = parking.parkVehicle(car);
            parking.removeVehicle(car, tokenNumber);
            System.out.println("slots available: " + parking.slotsAvailable());
        }
        for (int i = 0; i < 24; i++) {
            System.out.println("slots available: " + parking.slotsAvailable());
            parking.parkVehicle(motorcycle);
        }
        System.out.println("No of vehicles parked: " + parking.numberOfParkedVehicles());

    }
}
