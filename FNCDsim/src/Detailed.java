package FNCDsim.src;

import java.util.Objects;

//STRATEGY: methods for the detailed cleaning behavior used by interns
public class Detailed implements CleaningBehavior{

    public String getCleaningBehavior() {
        return "Detailing";
    }

    public boolean washVehicle(Vehicle car) {

        boolean changeState = false;

        int randomNum = Utility.findValue(1, 100);
        if (Objects.equals(car.getCleanliness(), Enums.Cleanliness.Dirty)) {
            if (randomNum <= 20){
                car.changeCarToSparkly();
                changeState = true;
            }
             else if (randomNum <= 60){
                car.changeCarToClean();
                changeState = true;
            }
        }
        else if (Objects.equals(car.getCleanliness(), Enums.Cleanliness.Clean)) {
            if (randomNum <= 5){
                car.changeCarToDirty();
                changeState = true;
            }
            else if (randomNum <= 40) { //clean --> sparkly
                car.changeCarToSparkly();
                changeState = true;
            }
        }
        return changeState;
    }
}
