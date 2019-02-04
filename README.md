# Cell
一个实验项目，探讨为什么当前微服务实践中服务划分成本、调试成本和部署成本会是最大的成本痛点，为什么服务粒度大小自动伸缩能在不引入新成本的前提下降低这些成本，并尝试通过实现一个代码生成套件和服务调用插件，实现一个服务粒度大小的自动化伸缩的微服务架构demo。
## 背景
为什么当前微服务实践中服务划分成本、调试成本和部署成本会是最大的成本痛点？那是因为对比一体化架构，微服务架构增加了这些需求，而且目前流行的微服务实践是通过人肉劳动来满足这些需求，而人是实现计算机系统时最贵且最不可靠的资源，于是导致微服务架构开发成本比一体化架构高非常多，下面将举例说明。
### 例子
本人于2015-2016年曾任职于国内一家5000员工量级、注册用户接近一亿量级的电话推销线下场所服务的公司。该企业通过内部CRM系统支持日均10W次用户联络和5%转化率的支付流程，同时维护数十个各种数据相互交联的实时报表（2016年数据）。2009-2017年间，该系统是一个一体化架构的系统，业务数据表都在同一个库内，维护该系统的程序员主要是通过sql实现各种功能，不能通过sql实现的需求或者sql很难实现的需求，比如跨机器跨库查询报表或者复杂逻辑报表则是另外写业务代码来实现，简而言之，业务沉降在数据库层面。产品经理提的需求也很灵活，除了功能需求，新增的报表需求也很多，而且逻辑常常穿透很多层面，比如电话沟通数据关联支付数据关联投诉数据，等等。随着业务发展，该系统分别实现了读写分离和对若该热点表的分表处理以满足需要。而在2015年前，此系统核心业务也基本保持了99.99%的可靠性。但是，该系统2015年-2016年的可靠性下降到了99.7%，此时支持该系统的数据库实例数也已达到了34台
