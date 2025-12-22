package com.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.controller.AcController;
import com.controller.CustomerController;
import com.model.entity.ACMode;
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

    public int globalTimeCounter = 0;
    public ACMode systemMode = ACMode.COOLING;

    @PostConstruct
    public void init() {
        System.out.println("正在初始化系统上下文...");
        resetSystem();
        System.out.println("系统上下文初始化完成");
    }

    /**
     * 新增：切换模式并重新初始化房间
     */
    public synchronized void switchModeAndReset(ACMode newMode) {
        this.systemMode = newMode;
        this.globalTimeCounter = 0;

        // 重新初始化所有Repository
        this.roomRepository = new InMemoryRoomRepository();
        this.customerRepository = new InMemoryCustomerRepository();
        this.orderRepository = new InMemoryAccommodationOrderRepository();
        this.accommodationBillRepository = new InMemoryAccommodationBillRepository();
        this.detailRepo = new InMemoryAcDetailRecordRepository();
        this.acBillRepo = new InMemoryAcBillRepository();

        // 根据模式初始化房间温度
        initializeRoomsForMode(newMode);

        // 重新初始化服务和调度器
        TemperatureModel temperatureModel = new TemperatureModel();
        this.billingService = new AcBillingServiceImpl(detailRepo, acBillRepo);
        this.scheduler = new DefaultScheduler(roomRepository, temperatureModel, billingService);

        AcService acService = new AcServiceImpl(roomRepository, scheduler);
        CustomerService customerService = new CustomerServiceImpl(customerRepository, roomRepository, orderRepository);
        this.billService = new BillServiceImpl(roomRepository, orderRepository, accommodationBillRepository);
        this.acReportService = new AcReportServiceImpl(detailRepo, acBillRepo);
        this.checkoutService = new CheckoutServiceImpl(roomRepository, billService, billingService, acReportService);

        this.acController = new AcController(acService);
        this.customerController = new CustomerController(customerService);

        if (this.billingService instanceof AcBillingServiceImpl) {
            ((AcBillingServiceImpl) this.billingService).clearAll();
        }

        System.out.println("系统模式切换完成：模式=" + this.systemMode + "，房间已重新初始化");
    }

    /**
     * 根据模式初始化房间
     */
    private void initializeRoomsForMode(ACMode mode) {
        double defaultTargetTemp = (mode == ACMode.HEATING) ? 23.0 : 25.0; // ★ 制热23度，制冷25度

        if (mode == ACMode.HEATING) {
            // 制热模式：低温初始化
            Room room101 = new Room("101", 10.0, 100.0);
            room101.setCheckInDays(0);
            room101.setTargetTemp(defaultTargetTemp); // ★ 设置默认目标温度
            roomRepository.save(room101);
            System.out.println("[SystemContext] 房间101 制热模式初始化: 当前温度=" + room101.getCurrentTemperature() + "°C, 目标温度="
                    + room101.getTargetTemperature() + "°C");

            Room room102 = new Room("102", 15.0, 125.0);
            room102.setCheckInDays(0);
            room102.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room102);
            System.out.println("[SystemContext] 房间102 制热模式初始化: 当前温度=" + room102.getCurrentTemperature() + "°C, 目标温度="
                    + room102.getTargetTemperature() + "°C");

            Room room103 = new Room("103", 18.0, 150.0);
            room103.setCheckInDays(0);
            room103.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room103);
            System.out.println("[SystemContext] 房间103 制热模式初始化: 当前温度=" + room103.getCurrentTemperature() + "°C, 目标温度="
                    + room103.getTargetTemperature() + "°C");

            Room room104 = new Room("104", 12.0, 200.0);
            room104.setCheckInDays(0);
            room104.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room104);
            System.out.println("[SystemContext] 房间104 制热模式初始化: 当前温度=" + room104.getCurrentTemperature() + "°C, 目标温度="
                    + room104.getTargetTemperature() + "°C");

            Room room105 = new Room("105", 14.0, 100.0);
            room105.setCheckInDays(0);
            room105.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room105);
            System.out.println("[SystemContext] 房间105 制热模式初始化: 当前温度=" + room105.getCurrentTemperature() + "°C, 目标温度="
                    + room105.getTargetTemperature() + "°C");

            System.out.println("制热模式：房间初始温度设置为低温（10-18℃），目标温度统一设为 " + defaultTargetTemp + "°C");
        } else {
            // 制冷模式：高温初始化
            Room room101 = new Room("101", 32.0, 100.0);
            room101.setCheckInDays(0);
            room101.setTargetTemp(defaultTargetTemp); // ★ 设置默认目标温度
            roomRepository.save(room101);

            Room room102 = new Room("102", 28.0, 125.0);
            room102.setCheckInDays(0);
            room102.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room102);

            Room room103 = new Room("103", 30.0, 150.0);
            room103.setCheckInDays(0);
            room103.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room103);

            Room room104 = new Room("104", 29.0, 200.0);
            room104.setCheckInDays(0);
            room104.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room104);

            Room room105 = new Room("105", 35.0, 100.0);
            room105.setCheckInDays(0);
            room105.setTargetTemp(defaultTargetTemp);
            roomRepository.save(room105);

            System.out.println("制冷模式：房间初始温度设置为高温（28-35℃），目标温度统一设为 " + defaultTargetTemp + "°C");
        }
    }

    public synchronized void resetSystem() {
        this.globalTimeCounter = 0;
        this.systemMode = ACMode.COOLING;

        this.roomRepository = new InMemoryRoomRepository();
        this.customerRepository = new InMemoryCustomerRepository();
        this.orderRepository = new InMemoryAccommodationOrderRepository();
        this.accommodationBillRepository = new InMemoryAccommodationBillRepository();
        this.detailRepo = new InMemoryAcDetailRecordRepository();
        this.acBillRepo = new InMemoryAcBillRepository();

        // 默认使用制冷模式初始化
        initializeRoomsForMode(ACMode.COOLING);

        TemperatureModel temperatureModel = new TemperatureModel();
        this.billingService = new AcBillingServiceImpl(detailRepo, acBillRepo);
        this.scheduler = new DefaultScheduler(roomRepository, temperatureModel, billingService);

        AcService acService = new AcServiceImpl(roomRepository, scheduler);
        CustomerService customerService = new CustomerServiceImpl(customerRepository, roomRepository, orderRepository);
        this.billService = new BillServiceImpl(roomRepository, orderRepository, accommodationBillRepository);
        this.acReportService = new AcReportServiceImpl(detailRepo, acBillRepo);
        this.checkoutService = new CheckoutServiceImpl(roomRepository, billService, billingService, acReportService);

        this.acController = new AcController(acService);
        this.customerController = new CustomerController(customerService);

        if (this.billingService instanceof AcBillingServiceImpl) {
            ((AcBillingServiceImpl) this.billingService).clearAll();
        }

        System.out.println("系统已重置：5个房间初始化完成，模式=" + this.systemMode);
    }
}
