package com.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.controller.AcController;
import com.controller.CustomerController;
import com.model.entity.Room;
import com.repository.AcBillRepository;
import com.repository.AcDetailRecordRepository;
import com.repository.AccommodationBillRepository;
import com.repository.AccommodationOrderRepository;
import com.repository.CustomerRepository;
import com.repository.InMemoryAcBillRepository;
import com.repository.InMemoryAcDetailRecordRepository;
import com.repository.InMemoryAccommodationBillRepository;
import com.repository.InMemoryAccommodationOrderRepository;
import com.repository.InMemoryCustomerRepository;
import com.repository.InMemoryRoomRepository;
import com.repository.RoomRepository;
import com.scheduler.DefaultScheduler;
import com.scheduler.Scheduler;
import com.service.AcBillingService;
import com.service.AcReportService;
import com.service.AcService;
import com.service.BillService;
import com.service.CheckoutService;
import com.service.CustomerService;
import com.service.impl.AcBillingServiceImpl;
import com.service.impl.AcReportServiceImpl;
import com.service.impl.AcServiceImpl;
import com.service.impl.BillServiceImpl;
import com.service.impl.CheckoutServiceImpl;
import com.service.impl.CustomerServiceImpl;
import com.simulation.TemperatureModel;

@Component
public class SystemContext {

    public RoomRepository roomRepository;
    public CustomerRepository customerRepository;
    public AccommodationOrderRepository orderRepository;
    public AccommodationBillRepository accommodationBillRepository;
    public AcDetailRecordRepository detailRepo;
    public AcBillRepository acBillRepo;

    public Scheduler scheduler;
    public AcController acController;
    public CustomerController customerController;
    public CheckoutService checkoutService;
    public BillService billService;
    public AcBillingService billingService;
    public AcReportService acReportService;

    // 系统时钟（分钟）
    public int globalTimeCounter = 0;

    @PostConstruct
    public void init() {
        System.out.println("正在初始化系统上下文...");
        resetSystem();
        System.out.println("系统上下文初始化完成");
    }

    public synchronized void resetSystem() {
        this.globalTimeCounter = 0;

        // 1. Repository 初始化
        this.roomRepository = new InMemoryRoomRepository();
        this.customerRepository = new InMemoryCustomerRepository();
        this.orderRepository = new InMemoryAccommodationOrderRepository();
        this.accommodationBillRepository = new InMemoryAccommodationBillRepository();
        this.detailRepo = new InMemoryAcDetailRecordRepository();
        this.acBillRepo = new InMemoryAcBillRepository();

        // 2. 初始化房间（对应测试用例配置）
        // ★ 修改：设置入住天数为0（不再设置checkInTime）
        Room room101 = new Room("101", 32.0, 100.0);
        room101.setCheckInDays(0);
        roomRepository.save(room101);

        Room room102 = new Room("102", 28.0, 125.0);
        room102.setCheckInDays(0);
        roomRepository.save(room102);

        Room room103 = new Room("103", 30.0, 150.0);
        room103.setCheckInDays(0);
        roomRepository.save(room103);

        Room room104 = new Room("104", 29.0, 200.0);
        room104.setCheckInDays(0);
        roomRepository.save(room104);

        Room room105 = new Room("105", 35.0, 100.0);
        room105.setCheckInDays(0);
        roomRepository.save(room105);

        // 3. Service 初始化
        TemperatureModel temperatureModel = new TemperatureModel();
        this.billingService = new AcBillingServiceImpl(detailRepo, acBillRepo);
        this.scheduler = new DefaultScheduler(roomRepository, temperatureModel, billingService);

        AcService acService = new AcServiceImpl(roomRepository, scheduler);
        CustomerService customerService = new CustomerServiceImpl(customerRepository, roomRepository, orderRepository);
        this.billService = new BillServiceImpl(roomRepository, orderRepository, accommodationBillRepository);
        this.acReportService = new AcReportServiceImpl(detailRepo, acBillRepo);
        this.checkoutService = new CheckoutServiceImpl(roomRepository, billService, billingService, acReportService);

        // 4. Controller 初始化
        this.acController = new AcController(acService);
        this.customerController = new CustomerController(customerService);

        // ★ 修改点2：清空计费数据
        if (this.billingService instanceof AcBillingServiceImpl) {
            ((AcBillingServiceImpl) this.billingService).clearAll();
        }

        System.out.println("系统已重置：5个房间初始化完成");
    }
}
