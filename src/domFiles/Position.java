package domFiles;


public class Position {

    private String DepCode;
    private String DepJob ;
    private String Description;

    public Position(){
        super();
    }


    public Position(String depCode, String depJob, String description) {
        DepCode = depCode;
        DepJob = depJob;
        Description = description;
    }

    public void setDepCode(String depCode) {
        DepCode = depCode;
    }

    public void setDepJob(String depJob) {
        DepJob = depJob;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDescription() {
        return Description;
    }

    public String getDepCode() {
        return DepCode;
    }

    public String getDepJob() {
        return DepJob;
    }

    @Override
    public String toString() {
        return "Position{" +
                "DepCode='" + DepCode + '\'' +
                ", DepJob='" + DepJob + '\'' +
                ", Description='" + Description + '\'' +
                ", Hash='" + this.hashCode() + '\'' + '}';
    }


    @Override
    public int hashCode() {
        return DepCode.hashCode() * DepJob.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Position inEl = (Position) obj;
        return this.hashCode() == inEl.hashCode();
    }
}
