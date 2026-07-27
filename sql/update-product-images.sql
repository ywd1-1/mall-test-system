START TRANSACTION;

UPDATE product
SET image_url = CASE id
  WHEN 1 THEN '/product-images/phone-x1.webp'
  WHEN 2 THEN '/product-images/laptop-pro.webp'
  WHEN 3 THEN '/product-images/wireless-earbuds.webp'
  WHEN 4 THEN '/product-images/keyboard-k87.webp'
  WHEN 5 THEN '/product-images/monitor-27.webp'
  WHEN 6 THEN '/product-images/usb-c-dock.webp'
  WHEN 7 THEN '/product-images/phone-case.webp'
  WHEN 8 THEN '/product-images/ergonomic-chair.webp'
  WHEN 9 THEN '/product-images/insulated-bottle.webp'
  WHEN 10 THEN '/product-images/portable-ssd-1tb.webp'
  WHEN 11 THEN '/product-images/smart-band.webp'
  ELSE '/product-images/test-product.webp'
END;

UPDATE order_item
SET product_image_url = CASE product_id
  WHEN 1 THEN '/product-images/phone-x1.webp'
  WHEN 2 THEN '/product-images/laptop-pro.webp'
  WHEN 3 THEN '/product-images/wireless-earbuds.webp'
  WHEN 4 THEN '/product-images/keyboard-k87.webp'
  WHEN 5 THEN '/product-images/monitor-27.webp'
  WHEN 6 THEN '/product-images/usb-c-dock.webp'
  WHEN 7 THEN '/product-images/phone-case.webp'
  WHEN 8 THEN '/product-images/ergonomic-chair.webp'
  WHEN 9 THEN '/product-images/insulated-bottle.webp'
  WHEN 10 THEN '/product-images/portable-ssd-1tb.webp'
  WHEN 11 THEN '/product-images/smart-band.webp'
  ELSE '/product-images/test-product.webp'
END;

COMMIT;
