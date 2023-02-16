/*
Fast Lookup Intern child class of Employee public methods:
hireIntern()
promoteInternToSales()
promoteInternToMechanic()
internQuit()
washCars()
 */

/*
OOAD principal example Identity:
The promotion methods have examples where object identity must be accounted for. Because of the unique
identity of an object when promoting an intern a new object of that promotion type must be created as
the Intern object is of type Intern and thus cannot be assigned a new type of Salesperson or Mechanic. To handel this
the Intern object values are used to populate the Salesperson object.

 */

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Intern extends Employee {

    Intern(){
        super("Intern", 100.0,175.0);
    }


    public Salesperson promoteInternToSales( Intern intern){
        Salesperson newSales = new Salesperson();
        newSales.name=intern.name;
        newSales.daysWorked= intern.daysWorked;
        newSales.incomeToDate = intern.incomeToDate;
        return newSales;
    }

    public Mechanic promoteInternToMechanic(Intern intern){
        Mechanic newMech = new Mechanic();
        newMech.name=intern.name;
        newMech.daysWorked= intern.daysWorked;
        newMech.incomeToDate = intern.incomeToDate;
       return newMech;
    }
    public boolean washCar(Vehicle car){
        int randomNum = ThreadLocalRandom.current().nextInt(1, 101);//generate probability of washing a car
        if(Objects.equals(car.getCleanliness(), "Dirty")){
            if(randomNum<=10) {//dirty-->sparkly
                car.changeCarToSparkly();
                return true;
            }
            if(randomNum<=90) {//dirty-->clean
                car.changeCarToClean();
                return true;
            }
        }
        else if(Objects.equals(car.getCleanliness(), "Clean")){
            if(randomNum<=5) {//clean --> dirty
                car.changeCarToDirty();
                return true;
            }
            if(randomNum<=35) {//clean --> sparkly
                car.changeCarToSparkly();
                return true;
            }
        }
        return false;
    }

}


