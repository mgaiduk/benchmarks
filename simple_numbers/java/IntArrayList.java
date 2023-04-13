public class IntArrayList {
    private int[] data;
    private int size;
    private int indexOfFirstElement;

    public IntArrayList() {
        this(10);
    }

    public IntArrayList(int initialCapacity) {
        data = new int[Math.max(initialCapacity, 10)];
        size = 0;
        indexOfFirstElement = 0;
    }

    public int size() {
        return size;
    }

    /**
     * Returns the index of the element in the internal array that is
     * {@code offset} positions away from the element at the index
     * {@code index}, wrapping around the array if necessary.
     * {@code offset} can be negative, in which case the distance will be
     * traveled to the left of the original element rather than to the right.
     * This method assumes that the passed index is valid and that the
     * absolute value of {@code offset} is not greater
     * than the length of the internal array. If these conditions are not
     * met, the returned index might not be valid.
     *
     * @param index  a valid index for the internal array
     * @param offset the distance from the element at index {@code index}; may be negative
     * @return the index of the array element at the specified distance
     */
    private int indexForOffset(int index, int offset) {
        int offsetIndex = index + offset; //might overflow
        if (data.length - index <= offset) { //includes cases where the previous line overflows
            offsetIndex -= data.length;
        } else if (offsetIndex < 0) { //cannot be the result of an overflow
            offsetIndex += data.length;
        }
        return offsetIndex;
    }

    public void ensureCapacity(final int capacity) {
        if (capacity > data.length) {
            int[] newData = new int[capacity];
            if (size != 0) {
                if (indexOfFirstElement <= indexForOffset(indexOfFirstElement, size - 1)) {
                    System.arraycopy(data, indexOfFirstElement, newData, 0, size);
                } else {
                    System.arraycopy(data, indexOfFirstElement, newData, 0, data.length - indexOfFirstElement);
                    System.arraycopy(data, 0, newData, data.length - indexOfFirstElement, indexOfFirstElement + size - data.length);
                }
            }
            data = newData;
            indexOfFirstElement = 0;
        }
    }

    /**
     * Converts an index for this {@code IntArrayList} to the
     * corresponding index for the internal array. This method
     * assumes that the passed index is valid. If it is not,
     * the returned index might not be valid as well.
     *
     * @param publicIndex A valid index for this {@code IntArrayList}
     * @return the index pointing to the corresponding element in the internal array
     */
    private int internalArrayIndex(int publicIndex) {
        int internalArrayIndex = publicIndex + indexOfFirstElement;
        if (internalArrayIndex < 0 || internalArrayIndex >= data.length) {
            internalArrayIndex -= data.length;
        }
        return internalArrayIndex;
    }

    public void add(final int value) {
        add(size, value);
    }

    public void add(final int index, final int value) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(index);
        }

        if (size == data.length) {
            if (data.length == Integer.MAX_VALUE) {
                throw new OutOfMemoryError();
            } else {
                int newCapacity = data.length + data.length / 2; // Grow by approx. 1.5x
                if (newCapacity < 0) { //overflow
                    newCapacity = Integer.MAX_VALUE;
                }
                ensureCapacity(newCapacity);
            }
        }

        int internalInsertionIndex = internalArrayIndex(index);
        if (index >= (size + 1) >>> 1) { //right-push
            moveRangeWrapping(data, internalInsertionIndex, 1, size - index);
        } else { //left-push
            internalInsertionIndex = indexForOffset(internalInsertionIndex, -1);
            moveRangeWrapping(data, indexOfFirstElement, -1, index);
            indexOfFirstElement = indexForOffset(indexOfFirstElement, -1);
        }
        data[internalInsertionIndex] = value;
        size++;
    }

    public void set(final int index, final int value) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(index);
        }
        data[internalArrayIndex(index)] = value;
    }

    public int get(final int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return data[internalArrayIndex(index)];
    }

    public int remove() {
        return remove(size - 1);
    }

    public int remove(int index) {
        final int result = get(index);

        int internalDeletionIndex = internalArrayIndex(index);

        if (index >= size / 2) { //right-side-pull
            moveRangeWrapping(data, indexForOffset(internalDeletionIndex, 1), -1, size - index - 1);
        } else { //left-side-pull
            moveRangeWrapping(data, indexOfFirstElement, 1, index);
            indexOfFirstElement = indexForOffset(indexOfFirstElement, 1);
        }

        size--;
        return result;
    }

    public void clear() {
        size = 0;
        indexOfFirstElement = 0;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            hashCode = 31 * hashCode + get(i);
        }
        return hashCode;
    }

    //moves a range without wrapping around
    private static void moveRange(int[] array, int sourceStartPosition, int destinationStartPosition, int length) {
        if (sourceStartPosition < 0 || destinationStartPosition < 0 || length < 0
                || sourceStartPosition > array.length - length
                || destinationStartPosition > array.length - length) {
            throw new IndexOutOfBoundsException();
        }
        int offset = destinationStartPosition - sourceStartPosition;
        if (offset > 0) {
            for (int i = destinationStartPosition + length - 1; i >= destinationStartPosition; i--) {
                array[i] = array[i - offset];
            }
        } else if (offset < 0) {
            for (int i = destinationStartPosition; i < destinationStartPosition + length; i++) {
                array[i] = array[i - offset];
            }
        }
    }

    //moves a range, wrapping around if necessary; requires that the foremost element
    //in the direction to be moved would not overwrite another element from the range to be moved
    private static void moveRangeWrapping(int[] array, int sourceStartPosition, int offset, int length) {
        if (length < 0 || length > array.length - Math.abs(offset)
                || sourceStartPosition < 0 || sourceStartPosition >= array.length) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > 0) {
            if (sourceStartPosition > array.length - length - offset) {
                if (sourceStartPosition > array.length - length) {
                    moveRange(array, 0, offset, sourceStartPosition + length - array.length);
                }
                int tempStartPosition = Math.max(array.length - offset, sourceStartPosition);
                moveRange(array, tempStartPosition, tempStartPosition + offset - array.length, (sourceStartPosition < array.length - length ? sourceStartPosition + length : array.length) - tempStartPosition);
            }
            if (array.length - offset > sourceStartPosition) {
                moveRange(array, sourceStartPosition, sourceStartPosition + offset, Math.min(length, array.length - offset - sourceStartPosition));
            }
        } else if (offset < 0) {
            //sourceEndPosition is from 1 to array.length, inclusive
            int sourceEndPosition = sourceStartPosition + length;
            if (sourceEndPosition > array.length || sourceEndPosition < 0) {
                sourceEndPosition -= array.length;
            }

            if (sourceEndPosition + offset < length) {
                if (sourceEndPosition < length) {
                    moveRange(array, sourceStartPosition, sourceStartPosition + offset, array.length - sourceStartPosition);
                }
                int tempStartPosition = Math.max(0, sourceEndPosition - length);
                moveRange(array, tempStartPosition, tempStartPosition + offset + array.length, Math.min(-offset, sourceEndPosition) - tempStartPosition);
            }
            if (sourceEndPosition + offset > 0) {
                int tempStartPosition = Math.max(-offset, sourceEndPosition - length);
                moveRange(array, tempStartPosition, tempStartPosition + offset, sourceEndPosition - tempStartPosition);
            }
        }
    }
}