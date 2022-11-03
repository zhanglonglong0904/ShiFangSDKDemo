package com.shifang.demo.bean;


public class StoredProductBean{

    public String code;
    public String name;
    public int pluNo;
    public String priceUnit = "kg";
    public double price;
    public double weight;

    public StoredProductBean(StoredProductBean productBean) {
        this.code = productBean.code;
        this.name = productBean.name;
        this.pluNo = productBean.pluNo;
        this.priceUnit = productBean.priceUnit;
        this.price = productBean.price;
        this.weight = productBean.weight;
    }

    public int getPluNo() {
        return pluNo;
    }

    public void setPluNo(int pluNo) {
        this.pluNo = pluNo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "StoredProductBean{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", pluNo=" + pluNo +
                '}';
    }
}
