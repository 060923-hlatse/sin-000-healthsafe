package co.wethinkcode.healthsafe;

public class WardRecord {

    private String WardId;
    private String wing;
    private String department;
    private Integer bedsAvailable;
    private String notes;

    public WardRecord(String WardId, String wing, String department, Integer bedsAvailable, String notes){
        this.WardId = WardId;
        this.wing = wing;
        this.department = department;
        this.bedsAvailable = bedsAvailable;
        this.notes = notes;
    }

    public String getWardId(){
        return WardId;
    }

    public String getWing(){
        return wing;
    }
    
    public String getDepartment(){
        return department;
    }

    public Integer getBedsAvailable(){
        return bedsAvailable;
    }

    public String getNotes(){
        return notes;
    }

    public void setWardId(String WardId){
        this.WardId = WardId; 
    }

    public void setWing(String wing){
        this.wing = wing;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public void setBedsAvailable(Integer bedsAvailable){
        this.bedsAvailable = bedsAvailable;
    }

    public void setNotes(String notes){
        this.notes = notes;
    }

}
