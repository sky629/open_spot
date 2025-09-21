-- Create review_images table for storing image metadata

CREATE TABLE location.review_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255),
    file_size BIGINT,
    content_type VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Check constraints
    CONSTRAINT ck_review_images_image_url_not_empty CHECK (TRIM(image_url) != ''),
    CONSTRAINT ck_review_images_file_size CHECK (file_size IS NULL OR file_size > 0),
    CONSTRAINT ck_review_images_display_order CHECK (display_order >= 1),

    -- Foreign key constraint to reviews table
    CONSTRAINT fk_review_images_review_id FOREIGN KEY (review_id) REFERENCES location.reviews(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_review_images_review_id ON location.review_images(review_id);
CREATE INDEX idx_review_images_display_order ON location.review_images(review_id, display_order);
CREATE INDEX idx_review_images_is_deleted ON location.review_images(is_deleted) WHERE is_deleted = false;

-- Add comments for documentation
COMMENT ON TABLE location.review_images IS 'Stores image metadata for review images';
COMMENT ON COLUMN location.review_images.review_id IS 'Reference to the review this image belongs to';
COMMENT ON COLUMN location.review_images.image_url IS 'URL or path to the stored image file';
COMMENT ON COLUMN location.review_images.original_filename IS 'Original filename when uploaded';
COMMENT ON COLUMN location.review_images.file_size IS 'Size of the image file in bytes';
COMMENT ON COLUMN location.review_images.content_type IS 'MIME type of the image (image/jpeg, image/png, etc.)';
COMMENT ON COLUMN location.review_images.display_order IS 'Order for displaying images in the review (1-based)';
COMMENT ON COLUMN location.review_images.is_deleted IS 'Soft delete flag for images';