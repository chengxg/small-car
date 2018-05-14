<template>
    <div :class="{'w3-show':isShow}" class="w3-modal" @click="clickMask" style="background-color: rgba(0, 0, 0, 0);">
        <div class="w3-modal-content w3-card-4" :style="contentStyle" :class="contentClass">
            <header class="w3-container w3-padding-large w3-blue" style="text-align:left;cursor: move;">
                <slot name="header"></slot>
            </header>
            <button v-if="showClose" class="w3-button w3-ripple w3-blue w3-display-topright w3-btn-close" @click="close">&times;</button>
            <slot></slot>
            <footer class="w3-container">
                <slot name="footer"></slot>
            </footer>
        </div>
    </div>
</template>

<script>
export default {
    name: 'MyDialog',
    data: function() {
        return {
            isShow: this.show
        }
    },
    props: {
        //窗口关闭
        beforeClose: Function,
        //点击遮罩是否关闭
        maskClose: {
            type: Boolean,
            default: false
        },
        //是否显示modal
        showModal: {
            type: Boolean,
            default: false
        },
        //是否显示
        show: {
            type: Boolean,
            default: true
        },
        //显示关闭按钮
        showClose: {
            type: Boolean,
            default: true
        },
        //dialog样式
        contentClass: {
            type: Array,
            default: function() {
                return [];
            }
        },
        //dialog宽度
        width: {
            type: String,
            default: "95%"
        },
        //锁定body滚动条
        lockScroll: {
            type: Boolean,
            default: true
        }
    },
    created: function() {
        this.isShow ? this.lockBodyScroll() : this.unLockBodyScroll();
    },
    computed: {
        contentStyle: function() {
            var style = {
                "width": this.width
            };
            return style;
        },
    },
    watch: {
        show: function() {
            this.isShow = this.show;
        },
        isShow: function() {
            this.isShow ? this.lockBodyScroll() : this.unLockBodyScroll();
        }
    },
    methods: {
        close: function() {
            if (this.beforeClose) {
                var close = this.beforeClose();
                if (close && close === false) {
                    this.isShow = true;
                    return;
                }
            }
            this.isShow = false;
        },
        clickMask: function(e) {
            if (e.target !== e.currentTarget) {
                return false;
            }
            if (this.maskClose) {
                this.close();
            }
        },
        drag: function(ev) {
            ev = ev || window.event;
            var dialogEl = ev.currentTarget.parentNode;
            var startX = ev.clientX;
            var startY = ev.clientY;
            var startOffsetLeft = dialogEl.offsetLeft;
            var startOffsetTop = dialogEl.offsetTop;
            dialogEl.style.position = "absolute";
            dialogEl.style.left = startOffsetLeft + "px";
            dialogEl.style.top = startOffsetTop + "px";

            document.addEventListener("mousemove", mousemove, false);
            document.addEventListener("mouseup", mouseup, false);
            document.addEventListener("touchmove", mousemove, false);
            document.addEventListener("touchend", mouseup, false);

            function mousemove(e) {
                e = e || window.event;

                dialogEl.style.left = (startOffsetLeft + e.clientX - startX) + "px";
                dialogEl.style.top = (startOffsetTop + e.clientY - startY) + "px";
                if (e.stopPropagation) {
                    e.stopPropagation();
                } else {
                    e.cancelBubble = true;
                }
            }

            function mouseup(e) {
                document.removeEventListener("mousemove", mousemove, false);
                document.removeEventListener("mouseup", mouseup, false);
                document.removeEventListener("touchmove", mousemove, false);
                document.removeEventListener("touchend", mouseup, false);
            }
        },
        lockBodyScroll: function() {
            if (this.lockScroll) {
                document.body.style.overflow = "hidden";
            }
        },
        unLockBodyScroll: function() {
            if (this.lockScroll) {
                document.body.style.overflow = "auto";
            }
        },
    },
    destroyed: function() {
        this.unLockBodyScroll();
    }
}
</script>

<style scope>

</style>
