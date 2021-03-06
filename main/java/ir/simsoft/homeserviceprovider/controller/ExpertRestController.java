package ir.simsoft.homeserviceprovider.controller;

import ir.simsoft.homeserviceprovider.exceptions.BusinessException;
import ir.simsoft.homeserviceprovider.repository.entity.*;
import ir.simsoft.homeserviceprovider.repository.enums.ConfirmationState;
import ir.simsoft.homeserviceprovider.repository.enums.OrderState;
import ir.simsoft.homeserviceprovider.repository.enums.PaymentStatus;
import ir.simsoft.homeserviceprovider.serviceclasses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping({"/expert"})
public class ExpertRestController {

    private ExpertService expertService;
    private UserService userService;
    private SubServicesService subServicesService;
    private OrdersService ordersService;
    private OfferService offerService;
    private BillService billService;
    private CommentsService commentsService;
    private WalletService walletService;
@Autowired
    public ExpertRestController(ExpertService expertService,
                                UserService userService, OrdersService ordersService,
                                SubServicesService subServicesService,
                                OfferService offerService,
                                BillService billService,
                                CommentsService commentsService,
                                WalletService walletService) {
        this.expertService = expertService;
        this.userService = userService;
        this.subServicesService = subServicesService;
        this.ordersService=ordersService;
        this.offerService=offerService;
        this.billService=billService;
        this.commentsService=commentsService;
        this.walletService=walletService;
    }

//    @GetMapping
//    public String hello(){
//        return "admin";
//    }

    /*******Rest Methods**************/
    @GetMapping("/allExperts")
    @ResponseBody
    public List<Expert> getUsers(){
        List<Expert> experts = expertService.findAllExperts();
        return experts;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public User getUserById(@PathVariable("id") int id){
        Optional<User> user =userService.getUserById(id);
        return user.get();
    }
    @PostMapping("/newExpert")
    @ResponseBody
    public ResponseEntity saveExpert(@RequestBody Expert expert){
        expert.setEnabled(true);
        Expert expert1 = expertService.registerExpert(expert);
        return ResponseEntity.ok("New Expert with ID " + expert1.getId() + " is added!");

    }

    @PutMapping(path = "/{id}")
    @ResponseBody
    public ResponseEntity updateUser(@PathVariable("id") int id) {
        Expert expertById = expertService.getExpertById(id);
        expertById.setConfirmationState(ConfirmationState.CONFIRMED);
        expertService.saveExpert(expertById);
        return ResponseEntity.ok("Expert with id= "+ id + " is updated.");
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity deleteAll(){
        userService.deleteAllExpert();
        return ResponseEntity.ok("All Experts are deleted!");
    }
    @DeleteMapping(path = "/{id}")
    @ResponseBody
    public ResponseEntity deleteUserById(@PathVariable("id")int id){
        userService.deleteUserById(id);
        return ResponseEntity.ok("Expert with id= "+ id + " is deleted.");
    }

    @PostMapping(path = "assignService/{email}")
    public ResponseEntity assignServiceToExpert(@PathVariable("email")String email
            ,@RequestParam(value = "subServiceName") String subServiceName){

        SubServices subServices = subServicesService.findSubServiceByName(subServiceName);
        System.out.println(subServices);
        if(subServices==null){
           throw new NullPointerException(BusinessException.nullPointerForSubService);
       }
       else {
           Expert expertByEmail = expertService.getExpertByEmail(email);
           if(expertByEmail!=null) {
               if (expertByEmail.getConfirmationState().equals(ConfirmationState.CONFIRMED)){
                   for (SubServices element : expertByEmail.getSubServicesList()) {
                       if (element.equals(subServices)) {
                           return ResponseEntity.badRequest().body("The Service of " + subServiceName + " already exists!!");
                       }
                   }
               expertByEmail.getSubServicesList().add(subServices);
//               subServices.getExpertList().add(expertByEmail);
//               subServicesService.updateSubService(subServices);
               expertService.updateExpert(expertByEmail);
               return ResponseEntity.ok("The Service of " + subServiceName + " is Successfully Assigned");
            }
               else{
                   return ResponseEntity.badRequest().body("The Expert is not Confirmed");
               }
           }

           else{
               throw new NullPointerException(BusinessException.nullPointerForExpert);
           }
       }
    }

    @PostMapping("/search")
    @ResponseBody
    public List<Expert> searchBySpecifiedField(@RequestBody Expert expert) {
        System.out.println(expert);
        return expertService.findBySpecifiedField(expert);
    }

    @GetMapping("/findAllExpertsOfOneSubService/{id}")
    @ResponseBody
    public List<Expert> findAllExpertsOfOneSubService(@PathVariable("id") int id){
        List<Expert> allExpertsBySubService = expertService.findAllExpertsBySubService(id);
        return allExpertsBySubService;
    }

    @DeleteMapping("/deleteOneExpertsOfOneSubService/{id}")
    @ResponseBody
    public ResponseEntity deleteExpertsOfOneSubService(@PathVariable("id") int id ,
                                                     @RequestParam(value = "expertEmail") String expertEmail){
        Expert expertByEmail = expertService.getExpertByEmail(expertEmail);
        for(int i=expertByEmail.getSubServicesList().size()-1;i>=0;i--){
            if(expertByEmail.getSubServicesList().get(i).getId().equals(id)){
                expertByEmail.getSubServicesList().remove(i);
                expertService.saveExpert(expertByEmail);
                return ResponseEntity.ok("the expert delete from subService");
            }
        }
        return ResponseEntity.badRequest().body("Not Found!");
    }

    @GetMapping("/getAssignedSubServices/{email}")
    public List<SubServices> getAssignedSubServices(@PathVariable("email") String email){

        Expert expertByEmail = expertService.getExpertByEmail(email);
        return expertByEmail.getSubServicesList();
    }
    @GetMapping("/getCommentsForDoneOrders/{email}")
    public List<Comments> getCommentsForDoneOrders(@PathVariable("email") String email){

        Expert expertByEmail = expertService.getExpertByEmail(email);
        return commentsService.getCommentsByExpert(expertByEmail);
    }
    @GetMapping("/getBillsByExpert/{email}")
    public List<Bill> getBillsByExpert(@PathVariable("email") String email){

        Expert expertByEmail = expertService.getExpertByEmail(email);
        return billService.getBillsByExpert(expertByEmail);
    }
    @GetMapping("/getTotalSalary/{email}")
    public Wallet getExpertWallet(@PathVariable("email") String email){
        Expert expertByEmail = expertService.getExpertByEmail(email);
        return walletService.getWalletByExpert(expertByEmail);
    }

    @GetMapping("/getOrdersByAssignedSubService/{subServiceId}")
    public List<Orders> getOrdersByAssignedSubService(@PathVariable("subServiceId")int id){

        List<Orders> allOrderBySubService = ordersService.getAllOrderBySubService(id);
        for(int i=allOrderBySubService.size()-1;i>=0;i--){
            if(allOrderBySubService.get(i).getOrderState().equals(OrderState.FINISHED) ||
                    allOrderBySubService.get(i).getOrderState().equals(OrderState.WAITING_FOR_EXPERT_TO_COME)){
                allOrderBySubService.remove(i);
            }
        }
        return allOrderBySubService;
    }
    @GetMapping("/getAllOrderComeHomeBySubService/{subServiceId}")
    public List<Orders> getAllOrderComeHomeBySubService(@PathVariable("subServiceId")int id,
                                                        @RequestParam("email")String email){
        Expert expertByEmail = expertService.getExpertByEmail(email);
        List<Orders> ordersList = ordersService.getAllOrderComeHomeBySubService(id);
        for(int i=ordersList.size()-1;i>=0;i--){
            Offer uniqueOffer = offerService.getOfferByUniqueExpertOrder(expertByEmail.getId(),ordersList.get(i).getId());
            if(uniqueOffer.getOrders().getId()!=ordersList.get(i).getId()){
                ordersList.remove(i);
            }
        }
        return ordersList;
    }
    @PutMapping("/finishOrderByOrderIdEmail/{email}")
    public ResponseEntity finishOrderByOrderIdEmail(@PathVariable("email")String email,
                                                    @RequestParam("orderId")int orderId){
        Expert expertByEmail = expertService.getExpertByEmail(email);
        Offer offerByUniqueExpertOrder = offerService.getOfferByUniqueExpertOrder(expertByEmail.getId(), orderId);
        if(Objects.isNull(offerByUniqueExpertOrder)){
            return ResponseEntity.badRequest().body("You Didnt Make an Offer For This Order");
        }
        if(offerByUniqueExpertOrder.getOrders().getOrderState().equals(OrderState.FINISHED)){
            return ResponseEntity.badRequest().body("The Job is Already Finished!");
        }
        if(!offerByUniqueExpertOrder.getOrders().getOrderState().equals(OrderState.WAITING_FOR_EXPERT_TO_COME)){
            return ResponseEntity.badRequest().body("You are not chosen yet for the job!");
        }
        offerByUniqueExpertOrder.getOrders().setOrderState(OrderState.FINISHED);
        offerService.saveOffer(offerByUniqueExpertOrder);
        Bill bill=new Bill();
        bill.setAmount(offerByUniqueExpertOrder.getProposedPrice());
        bill.setExpert(offerByUniqueExpertOrder.getExpert());
        bill.setOrders(offerByUniqueExpertOrder.getOrders());
        bill.setPaymentStatus(PaymentStatus.Not_YET_PAID);
        billService.saveBill(bill);
        return ResponseEntity.ok("The Order is Finished");
    }


//    @GetMapping("/getExpertID/{email}")
//    public List<SubServices> getAssignedSubServices(@PathVariable("email") String email){
//        Expert expertById = expertService.getExpertById(id);
//        return expertById.getSubServicesList();
//    }
}
