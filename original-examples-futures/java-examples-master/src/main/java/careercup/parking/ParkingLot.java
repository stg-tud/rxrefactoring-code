package careercup.parking;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {

    private List<VehiclePosition> parked = new ArrayList<VehiclePosition>();
    private static final int maxCapacity = 50;
    private Slot[] slots = new Slot[maxCapacity];
    private static int numFreeSlots = maxCapacity;

    public ParkingLot() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(i);
        }
    }

    public Slot parkVehicle(Vehicle vehicle) throws Exception {
        Slot slot = null;
        synchronized (this) {
            slot = availableSlot();
            if (slot == null) {
                throw new Exception("Parking Full, Slot not available");
            }
            numFreeSlots--;
            VehiclePosition vehiclePos = new VehiclePosition(vehicle, slot);
            parked.add(vehiclePos);
            System.out.println(vehicle.getClass().getSimpleName() + " Parked at "
                    + vehiclePos.getPosition().slotPos);
            return slot;
        }
    }

    public void removeVehicle(Vehicle vehicle, Slot slot) {
        VehiclePosition vehiclePos = new VehiclePosition(vehicle, slot);
        synchronized (this) {
            parked.remove(vehiclePos);
            freeSlot(slot);
            System.out.println(vehicle.getClass().getSimpleName() + " Removed from "
                    + vehiclePos.getPosition().slotPos);
        }
    }

    public Slot availableSlot() {
        for (Slot slot : slots) {
            // System.out.println(slot);
            if (slot.available) {
                slot.available = false;
                return slot;
            }
        }
        return null;
    }

    public void freeSlot(Slot slot) {
        numFreeSlots++;
        slot.available = true;
    }

    public int slotsAvailable() {
        return numFreeSlots;
    }

    public int numberOfParkedVehicles() {
        return maxCapacity - numFreeSlots;
    }
}

class Slot {
    boolean available;
    int slotPos;

    public Slot(int pos) {
        available = true;
        this.slotPos = pos;
    }

    public String toString() {
        return "Available :" + available + " Slot Position: " + slotPos;
    }
}

class VehiclePosition {

    Vehicle vehicle;
    Slot position;

    public VehiclePosition(Vehicle vehicle, Slot pos) {
        this.vehicle = vehicle;
        this.position = pos;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Slot getPosition() {
        return position;
    }

    public void setPosition(Slot position) {
        this.position = position;
    }

}
