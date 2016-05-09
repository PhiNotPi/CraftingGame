public class Tag {

  String name;
  Boolean bool;
  Double min;
  Double max;
  public Tag(String name){
    this.name = name;
  }
  public Tag(String name, boolean bool){
    this.name = name;
    this.bool = bool;
  }
  public Tag(String name, double val){
    this.name = name;
    this.min = val;
    this.max = val;
  }
  public Tag(String name, double min, double max){
    this.name = name;
    this.min = min;
    this.max = max;
  }
  public String fieldName(){
    return TagData.getFieldName(name);
  }
  public String toString() {
    if(this.min != null){
      return "["+name+":"+min+"]";
    }
    if(this.bool != null){
      return "["+name+":"+bool+"]";
    }
      return "["+fieldName()+":"+name+"]";
  }

}
