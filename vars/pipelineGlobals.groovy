def getAccountID(String environment){
    switch(environment) { 
        case 'dev': 
            return "537124943253"
        ... 
        case 'qa': 
            return "537124943253"
        ... 
        case 'uat': 
            return "537124943253"
        ... 
         case 'pre-prod': 
            return "537124943253"
        ... 
         case 'prod': 
            return "537124943253"
        ... 
        default:
            return "nothing"
    } 
}
