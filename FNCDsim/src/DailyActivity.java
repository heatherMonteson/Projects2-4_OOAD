package FNCDsim.src;
/*OOAD Principal
Identity: the end of the sell cars method uses the unique identity of an object to remove a car from the inventory. The
API method remove for arraylists looks for an object matching the identity of the given object to remove it from the list:
sellCar=salesPerson.sellCar(customer, sellableCars)
inventory.remove(sellCar);
*/

//Daily activity carried out at the FNCD broken up by type
//Accessing and updating static variables from the simulation class
//NOTE: make monetary updates through the getter and setters of the FNCD simulation class
//New: getters and setter for both dealership inventory/staff lists
import java.util.ArrayList;
import java.util.Objects;

public abstract class DailyActivity {

}

//OO pattern Command: implemented in FNCDsim(run function), DailyActivity(Online Shopping function), Employee(reciever
//class for many concrete commands), CLSim, CLI, Command, StringCommand, CommandInvoker, any file starting with
//"Ask" is a concrete command. The simplified flow through the command pattern is as follows:
//FLOW: User --> CLI --> Invoker --> Reciever

abstract class OnlineShopping extends DailyActivity{
    public static void onlineShop(String carName, ArrayList<Enums.AddOns> addOns, Employee salesperson){
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("Selling cars at "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );

        //we are given the car name and salesperson, and from that we locate the vehicle
        Vehicle vehicleRemoval = null; // initialize to null
        for (Vehicle vehicle : FNCDsim.inventory()) {
            if (vehicle.getName().equals(carName)) {

                //decorate
                Vehicle decoratedCar = vehicle;
                if(addOns.contains(Enums.AddOns.Road_Side_Rescue_Coverage))
                    decoratedCar=new RoadRescueCoverage(decoratedCar);
                if(addOns.contains(Enums.AddOns.Extended_Warranty))
                    decoratedCar=new ExtendedWarranty(decoratedCar);
                if(addOns.contains(Enums.AddOns.Satellite_Radio))
                    decoratedCar=new SatelliteRadio(decoratedCar);
                if(addOns.contains(Enums.AddOns.Undercoating))
                    decoratedCar= new Undercoating(decoratedCar);

                //buy car
                double price = decoratedCar.getSalePrice();
                //adding money from sale to FNCD accounts
                FNCDsim.addSales(price);
                salesperson.payBonus(FNCDsim.getFunds(vehicle.getSaleBonus()));
                vehicleRemoval = vehicle;

                System.out.println("Congratulations, you have bought the car: "+ vehicle.getName());
                //tracker print out
                FNCDsim.broker.out(Enums.EventType.Selling,"Salesperson "+ salesperson.getName()+" sold the "+ vehicle.getName()+ " "+vehicle.getType() +" for $"+ decoratedCar.getSalePrice() + " " + decoratedCar.getAddOnDes() +
                 " (earned a $"+ vehicle.getSaleBonus()+" bonus)", vehicle.getSaleBonus(),decoratedCar.getSalePrice());
                break;
            }
        }
        //remove from inventory after iteration
        if (vehicleRemoval != null) {
            FNCDsim.removeVehicle(vehicleRemoval);
        }
        else{
            System.out.println("ERROR: You have entered a vehicle that is not in inventory");
        }
    }
}


//////////////////////////////////////////////////////////////////////
abstract class OpenShop extends DailyActivity{
    public static int setCarNum = 6;
    public static int setStaffNum = 3;
    public static void openShop(){
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out( "Opening "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out(Enums.EventType.Opening, "Starting today with a budget of $" + FNCDsim.getBalance(), FNCDsim.getBalance());

        //Add employees as needed by type
        for(Enums.StaffType type: Enums.StaffType.values()) {
            ArrayList<Employee> typeList= Employee.getStaffByType(FNCDsim.currentStaff(), type);
            if(typeList.size()<setStaffNum)
                addEmployee(typeList.size(), type);
        }

        //buy vehicles as needed, funds removed in addInventory method (6 of each)
        for(Enums.VehicleType type: Enums.VehicleType.values()) {
            ArrayList<Vehicle> typeList= Vehicle.getVehiclesByType(FNCDsim.inventory(), type);
            if(typeList.size()<setCarNum)
                addInventory(typeList.size(), type);
        }
    }

    private static void addEmployee(int size, Enums.StaffType type) {
        Employee employee;
        //OO factory pattern: using the Staff factory to create new instances of staff
        StaffFactory factory = new HireStaff();
        //get the inventory based on type
        for(int i =0 ; i<setStaffNum-size; i++) {
            employee = factory.hireStaff(type);
            if(employee!=null)
            {
                FNCDsim.broker.out(Enums.EventType.Hiring,"Hired new "+ type + " " +employee.getName());
                FNCDsim.addStaff(employee);
            }
            else//error making employee type
                FNCDsim.broker.errorOut("type "+ type + " employees not added");
        }
    }

    //add one new vehicle based on type to current inventory list
    private static void addInventory(int size, Enums.VehicleType type) {
        Vehicle vehicle;
        //OO factory pattern: using the Vehicle factory to create new instances of vehicles
        VehicleFactory factory = new MakeCars();
        //get the inventory based on type
        for(int i =0 ; i<setCarNum-size; i++) {
            vehicle=factory.buyCars(type);
            //If type given matches the current types to add then add/remove cost from funds
            if(vehicle!=null)
            {
                FNCDsim.addVehicle(vehicle);
                FNCDsim.getFunds(vehicle.getCost());
                FNCDsim.broker.out(Enums.EventType.Buying,"New "+ vehicle.getType() +" added to inventory: "+ vehicle.getCondition() + " "+vehicle.getCleanliness() +" "+ vehicle.getName() + " for $"+ vehicle.getCost() + " cost.",  vehicle.getCost()  );
            }
            else//error making employee
                FNCDsim.broker.errorOut("type "+ type + " cars not added");
        }
    }
}

///////////////////////////////////////////////////////////////////////
abstract class WashCars extends DailyActivity{
    public static void washCars(){
        if(FNCDsim.inventory().size()==0){
            FNCDsim.broker.out("No cars to wash" );
            return;
        }
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("Washing cars "+FNCDsim.getCurrentDealership()+"..." );
        FNCDsim.broker.out("////////////////////////////////" );

        ArrayList<Vehicle>cleanCars= new ArrayList<Vehicle>();
        ArrayList<Vehicle>dirtyCars= new ArrayList<Vehicle>();
        Vehicle washedCar;
        int j=0;

        for (Vehicle vehicle : FNCDsim.inventory()) { //find all clean and dirty cars in inventory that need to be washed
            if (Objects.equals(vehicle.getCleanliness(), Enums.Cleanliness.Clean))
                cleanCars.add(vehicle);
            else if (Objects.equals(vehicle.getCleanliness(),  Enums.Cleanliness.Dirty))
                dirtyCars.add(vehicle);
        }

        ArrayList<Employee> interns = Employee.getStaffByType(FNCDsim.currentStaff(), Enums.StaffType.Intern);
        //cycle through all interns 2x first washing random dirty then clean cars
        //UPDATED from 2.2: only allow one attempt to wash a car, remove from list after attempt
        while (j<=1 && (dirtyCars.size()>0 || cleanCars.size()>0)){
            for(Employee intern:interns){
                Intern washer = (Intern) intern;
                if(dirtyCars.size()>0) {
                    washedCar = dirtyCars.get(Utility.findValue(0, dirtyCars.size() - 1));
                    washer.internWashCar(washedCar);
                    dirtyCars.remove(washedCar);
                }
                else if(cleanCars.size()>0){
                    washedCar= cleanCars.get(Utility.findValue(0, cleanCars.size()-1));
                    washer.internWashCar(washedCar);
                    cleanCars.remove(washedCar);
                }
            }
            j++;
        }
    }

}

//////////////////////////////////////////////
abstract class FixCars extends DailyActivity{
    public static void fixCars(){
        if(FNCDsim.inventory().size()==0){
            FNCDsim.broker.out("No cars to fix");
            return;
        }
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("Fixing cars at "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );

        ArrayList<Vehicle>carsToFix= new ArrayList<Vehicle>();
        ArrayList<Employee> mechanics = Employee.getStaffByType(FNCDsim.currentStaff(), Enums.StaffType.Mechanic);
        int j=0;

        //select only broken and used cars for them to fix.
        for(Vehicle vehicle: FNCDsim.inventory()){
            if(Objects.equals(vehicle.getCondition(), Enums.Condition.Broken) || Objects.equals(vehicle.getCondition(), Enums.Condition.Used))
                carsToFix.add(vehicle);
        }
        //loop though mechanics 2x to have them fix cars
        //UPDATE from 2.2: only allowing one fix attempt on a car, removing from list of repairable cars after attempt
        while (j<=1 && (carsToFix.size()>0)) {
            for(Employee mech: mechanics){
                Mechanic mechanic = (Mechanic) mech;
                if(carsToFix.size()>0){
                    Vehicle car = carsToFix.get(Utility.findValue(0, carsToFix.size()-1));
                    mechanic.fixCar(car);
                    carsToFix.remove(car);
                }
            }
            j++;
        }
    }
}

//////////////////////////////////////////////
abstract class SellCars extends DailyActivity{

    public static void sellCars(int today){
        if(FNCDsim.inventory().size()==0){
            FNCDsim.broker.out("No cars to sell");
            return;
        }
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("Selling cars at "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );

        Customer customers= new Customer();
        ArrayList <Vehicle> sellableCars = new ArrayList<Vehicle>();
        ArrayList<Employee> salesPeople = Employee.getStaffByType(FNCDsim.currentStaff(), Enums.StaffType.Salesperson);
        Vehicle sellCar;
        int numCustomers=0;

        //create num customers based on day of the week and access with array
        if(today==6 || today== 5)//friday and satuday 2--> 8 customers show up
            numCustomers = Utility.findValue(2, 8);
        else //all other days 0-->5 show up
            numCustomers =Utility.findValue(0, 5);

        customers = new Customer(numCustomers);

        ArrayList<Customer> allCustomers = customers.getCustomers();
        FNCDsim.broker.out(Enums.EventType.Selling, "There are "+ allCustomers.size()+" customers looking to purchase vehicles today");
        if(allCustomers.size()==0)
            return;
        //Find vehicles to sell don't sell broken cars
        for(Vehicle car: FNCDsim.inventory()){
            if(!Objects.equals(car.getCondition(), Enums.Condition.Broken))
                sellableCars.add(car);
        }

        //loop through customers and pair them with a sales person
        for(Customer customer: allCustomers){
            //determine if sales person makes a sale, remove from inventory if so and update all other info
            Salesperson salesPerson = (Salesperson) salesPeople.get(Utility.findValue(0, salesPeople.size()-1));
            sellCar=salesPerson.sellCar(customer, sellableCars);

            if(sellCar!=null){
                sellableCars.remove(sellCar);
                FNCDsim.removeVehicle(sellCar);
            }
        }
    }
}

//////////////////////////////////////////////
abstract class Race extends DailyActivity{

    public static void race() {
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("Racing at "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );


        //select and collect type to race
        ArrayList<Enums.VehicleType> types= RaceCar.getRaceTypes();
        Enums.VehicleType type = types.get(Utility.findValue(0, types.size()-1));
        ArrayList<Vehicle> raceCarsAll= RaceCar.getRaceCarsByType(FNCDsim.inventory(), type);

        if(raceCarsAll.size()==0){//not enough cars
            FNCDsim.broker.out(Enums.EventType.Racing, "The FNCD is unable to participate in races today as there are no " +type + "s");
            return;
        }

        ArrayList<Driver> allDrivers = new ArrayList<>();
        for(Employee driver: Employee.getStaffByType(FNCDsim.currentStaff(), Enums.StaffType.Driver)){//get all drivers
            allDrivers.add((Driver) driver);
        }

        ArrayList<Driver> drivers = new ArrayList<>();
        ArrayList<Vehicle> raceCars=new ArrayList<>();
        for(int i=0; i<raceCarsAll.size() && i < 3 && i < allDrivers.size(); i++){//get the correct number of drivers and cars (no more than 3)
            drivers.add(allDrivers.get(i));
            raceCars.add(raceCarsAll.get(i));
            FNCDsim.broker.out(Enums.EventType.Racing, "Driver " +drivers.get(i).getName() + " is racing the " + raceCars.get(i).getType()+" "+raceCars.get(i).getName());
        }
        RaceCar.doRace(drivers, raceCars);
    }
}

//////////////////////////////////////////////
abstract class EndOfDay extends DailyActivity{

    public static void endOfDay(){
        FNCDsim.broker.out("////////////////////////////////" );
        FNCDsim.broker.out("End of day at "+FNCDsim.getCurrentDealership()+"...");
        FNCDsim.broker.out("////////////////////////////////" );


        //update everyone's pay and days worked
        //remove daily pay from the budget
        for(Employee employee:FNCDsim.currentStaff() ){
            employee.addDayWorked();
            FNCDsim.getFunds(employee.getSalary());
            FNCDsim.broker.out(Enums.EventType.PaySalary, employee.getSalary());
            employee.payDailyRate();
        }

        //All quitting activity during the end of day
        staffQuitByType(Enums.StaffType.Intern);//intern quits, just gets removed from list
        Employee promoted = null;
        if(staffQuitByType(Enums.StaffType.Salesperson)){//if sales quits, promote intern
            promoted=promoteInternTo(Enums.StaffType.Salesperson);

            if(promoted!=null)//announce promotion
                FNCDsim.broker.out(Enums.EventType.Promoting,"Intern " + promoted.getName()+ " was promoted to "+promoted.getType() +" has a new daily salary of $"+promoted.getSalary());
        }
        if(staffQuitByType(Enums.StaffType.Mechanic)) {//if mechanic quits, promote intern
            promoted = promoteInternTo(Enums.StaffType.Mechanic);
            if (promoted != null)//announce promotion
                FNCDsim.broker.out(Enums.EventType.Promoting, "Intern " + promoted.getName() + " was promoted to " + promoted.getType() + " has a new daily salary of $" + promoted.getSalary());
        }
    }

    //Selects a random intern to promote based on type of promotion
    private static Employee promoteInternTo(Enums.StaffType newPosition){
        //select intern
        ArrayList<Employee> staffByType = Employee.getStaffByType(FNCDsim.currentStaff(), Enums.StaffType.Intern);
        Intern intern = (Intern) staffByType.get(Utility.findValue(0, staffByType.size()-1));

        //get new promoted intern based on position
        Employee newEmployee = intern.promoteIntern(newPosition);

        //change employee position in staff array
        if(newEmployee!=null){
            FNCDsim.removeIntern(intern);
            FNCDsim.addStaff(newEmployee);
        }
        else
            FNCDsim.broker.errorOut("Error promoting intern");
        return newEmployee;
    }

    private static boolean staffQuitByType(Enums.StaffType type){
        //if employee quits remove from current staff and add to departed
        if(Employee.employeeQuit()){
            try {
                ArrayList<Employee> staffByType = Employee.getStaffByType(FNCDsim.currentStaff(), type);
                int randEmp=Utility.findValue(0, staffByType.size()-1);
                FNCDsim.broker.out(Enums.EventType.Quitting, type+" staff " + staffByType.get(randEmp).getName() + " has quit");
                FNCDsim.removeStaff(staffByType.get(randEmp));
                return true;
            }
            catch (Exception e){
                FNCDsim.broker.errorOut("Error with staff quit");
            }

        }
        return false;
    }

}